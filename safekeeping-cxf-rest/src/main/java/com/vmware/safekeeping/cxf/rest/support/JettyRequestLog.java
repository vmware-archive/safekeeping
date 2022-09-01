package com.vmware.safekeeping.cxf.rest.support;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.pathmap.PathMappings;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.annotation.ManagedAttribute;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import com.vmware.safekeeping.common.Utility;

public class JettyRequestLog extends ContainerLifeCycle implements RequestLog {

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(JettyRequestLog.class.getName());
    private static ThreadLocal<StringBuilder> _buffers = ThreadLocal.withInitial(() -> new StringBuilder(256));

    private String[] ignorePaths;
    private boolean extended;
    private PathMappings<String> ignorePathMap;
    private boolean logLatency;
    private boolean logCookies;
    private boolean logServer;
    private boolean preferProxiedForAddress;
    private DateCache logDateCache;
    private String logDateFormat = "dd/MMM/yyyy:HH:mm:ss Z";
    private Locale logLocale = Locale.getDefault();
    private String logTimeZone = "GMT";

    private final boolean httpDebug;

    public JettyRequestLog(boolean httpDebug) {
        this.httpDebug = httpDebug;
        setExtended(true);
        setLogCookies(false);
        setLogTimeZone("GMT");
    }

    private void append(final StringBuilder buf, final String s) {
        if ((s == null) || (s.length() == 0)) {
            buf.append('-');
        } else {
            buf.append(s);
        }
    }

    /**
     * Set up request logging and open log file.
     *
     * @see org.eclipse.jetty.util.component.AbstractLifeCycle#doStart()
     */
    @Override
    protected synchronized void doStart() throws Exception {
        if (this.logDateFormat != null) {
            this.logDateCache = new DateCache(this.logDateFormat, this.logLocale, this.logTimeZone);
        }

        if ((this.ignorePaths != null) && (this.ignorePaths.length > 0)) {
            this.ignorePathMap = new PathMappings<>();
            for (final String _ignorePath : this.ignorePaths) {
                this.ignorePathMap.put(_ignorePath, _ignorePath);
            }
        } else {
            this.ignorePathMap = null;
        }

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        this.logDateCache = null;
        super.doStop();
    }

    /**
     * Extract the user authentication
     *
     * @param request The request to extract from
     * @return The string to log for authenticated user.
     */
    protected String getAuthentication(final Request request) {
        final Authentication authentication = request.getAuthentication();

        if (authentication instanceof Authentication.User) {
            return ((Authentication.User) authentication).getUserIdentity().getUserPrincipal().getName();
        }

        return null;
    }

    /**
     * Retrieve the request paths that will not be logged.
     *
     * @return array of request paths
     */
    public String[] getIgnorePaths() {
        return this.ignorePaths;
    }

    /**
     * Retrieve log cookies flag
     *
     * @return value of the flag
     */
    public boolean getLogCookies() {
        return this.logCookies;
    }

    /**
     * Retrieve the timestamp format string for request log entries.
     *
     * @return timestamp format string.
     */
    public String getLogDateFormat() {
        return this.logDateFormat;
    }

    /**
     * Retrieve log request processing time flag.
     *
     * @return value of the flag
     */
    public boolean hasLogLatency() {
        return this.logLatency;
    }

    /**
     * Retrieve the locale of the request log.
     *
     * @return locale object
     */
    public Locale getLogLocale() {
        return this.logLocale;
    }

    /**
     * Retrieve log hostname flag.
     *
     * @return value of the flag
     */
    public boolean hasLogServer() {
        return this.logServer;
    }

    /**
     * Retrieve the timezone of the request log.
     *
     * @return timezone string
     */
    @ManagedAttribute("the timezone")
    public String getLogTimeZone() {
        return this.logTimeZone;
    }

    /**
     * Retrieved log X-Forwarded-For IP address flag.
     *
     * @return value of the flag
     */
    public boolean getPreferProxiedForAddress() {
        return this.preferProxiedForAddress;
    }

    /**
     * Is logging enabled
     *
     * @return true if logging is enabled
     */
    protected boolean isEnabled() {
        return true;
    }

    /**
     * Retrieve the extended request log format flag.
     *
     * @return value of the flag
     */
    @ManagedAttribute("use extended NCSA format")
    public boolean isExtended() {
        return this.extended;
    }

    /**
     * @return true if logging dispatches
     * @deprecated use {@link StatisticsHandler}
     */
    @Deprecated
    public boolean isLogDispatch() {
        return false;
    }

    /**
     * Writes the request and response information to the output stream.
     *
     * @see org.eclipse.jetty.server.RequestLog#log(Request, Response)
     */
    @Override
    public void log(final Request request, final Response response) {
        try {
            if ((this.ignorePathMap != null) && (this.ignorePathMap.getMatch(request.getRequestURI()) != null)) {
                return;
            }

            if (!isEnabled()) {
                return;
            }

            final StringBuilder buf = _buffers.get();
            buf.setLength(0);

            if (this.logServer) {
                append(buf, request.getServerName());
                buf.append(' ');
            }

            String addr = null;
            if (this.preferProxiedForAddress) {
                addr = request.getHeader(HttpHeader.X_FORWARDED_FOR.toString());
            }

            if (addr == null) {
                addr = request.getRemoteAddr();
            }

            buf.append(addr);
            buf.append(" - ");

            final String auth = getAuthentication(request);
            append(buf, auth == null ? "-" : auth);

            buf.append(" [");
            if (this.logDateCache != null) {
                buf.append(this.logDateCache.format(request.getTimeStamp()));
            } else {
                buf.append(request.getTimeStamp());
            }

            buf.append("] \"");
            append(buf, request.getMethod());
            buf.append(' ');
            append(buf, request.getOriginalURI());
            buf.append(' ');
            append(buf, request.getProtocol());
            buf.append("\" ");

            final int status = response.getCommittedMetaData().getStatus();
            if (status >= 0) {
                buf.append((char) ('0' + ((status / 100) % 10)));
                buf.append((char) ('0' + ((status / 10) % 10)));
                buf.append((char) ('0' + (status % 10)));
            } else {
                buf.append(status);
            }

            final long written = response.getHttpChannel().getBytesWritten();
            if (written >= 0) {
                buf.append(' ');
                if (written > 99999) {
                    buf.append(written);
                } else {
                    if (written > 9999) {
                        buf.append((char) ('0' + ((written / 10000) % 10)));
                    }
                    if (written > 999) {
                        buf.append((char) ('0' + ((written / 1000) % 10)));
                    }
                    if (written > 99) {
                        buf.append((char) ('0' + ((written / 100) % 10)));
                    }
                    if (written > 9) {
                        buf.append((char) ('0' + ((written / 10) % 10)));
                    }
                    buf.append((char) ('0' + ((written) % 10)));
                }
                buf.append(' ');
            } else {
                buf.append(" - ");
            }

            if (this.extended) {
                logExtended(buf, request, response);
            }

            if (this.logCookies) {
                final Cookie[] cookies = request.getCookies();
                if ((cookies == null) || (cookies.length == 0)) {
                    buf.append(" -");
                } else {
                    buf.append(" \"");
                    for (int i = 0; i < cookies.length; i++) {
                        if (i != 0) {
                            buf.append(';');
                        }
                        buf.append(cookies[i].getName());
                        buf.append('=');
                        buf.append(cookies[i].getValue());
                    }
                    buf.append('\"');
                }
            }

            if (this.logLatency) {
                final long now = System.currentTimeMillis();

                if (this.logLatency) {
                    buf.append(' ');
                    buf.append(now - request.getTimeStamp());
                }
            }

            final String log = buf.toString();
            write(log);
        } catch (final IOException e) {
            Utility.logWarning(logger, e);
        }
    }

    /**
     * Writes extended request and response information to the output stream.
     *
     * @param b        StringBuilder to write to
     * @param request  request object
     * @param response response object
     * @throws IOException if unable to log the extended information
     */
    protected void logExtended(final StringBuilder b, final Request request, final Response response)
            throws IOException {
        final String referer = request.getHeader(HttpHeader.REFERER.toString());
        if (referer == null) {
            b.append("\"-\" ");
        } else {
            b.append('"');
            b.append(referer);
            b.append("\" ");
        }

        final String agent = request.getHeader(HttpHeader.USER_AGENT.toString());
        if (agent == null) {
            b.append("\"-\"");
        } else {
            b.append('"');
            b.append(agent);
            b.append('"');
        }
    }

    /**
     * Set the extended request log format flag.
     *
     * @param extended true - log the extended request information, false - do not
     *                 log the extended request information
     */
    public void setExtended(final boolean extended) {
        this.extended = extended;
    }

    /**
     * Set request paths that will not be logged.
     *
     * @param ignorePaths array of request paths
     */
    public void setIgnorePaths(final String[] ignorePaths) {
        this.ignorePaths = ignorePaths;
    }

    /**
     * Controls logging of the request cookies.
     *
     * @param logCookies true - values of request cookies will be logged, false -
     *                   values of request cookies will not be logged
     */
    public void setLogCookies(final boolean logCookies) {
        this.logCookies = logCookies;
    }

    /**
     * Set the timestamp format for request log entries in the file. If this is not
     * set, the pre-formated request timestamp is used.
     *
     * @param format timestamp format string
     */
    public void setLogDateFormat(final String format) {
        this.logDateFormat = format;
    }

    /**
     * @param value true to log dispatch
     * @deprecated use {@link StatisticsHandler}
     */
    @Deprecated
    public void setLogDispatch(final boolean value) {
    }

    /**
     * Controls logging of request processing time.
     *
     * @param logLatency true - request processing time will be logged false -
     *                   request processing time will not be logged
     */
    public void setLogLatency(final boolean logLatency) {
        this.logLatency = logLatency;
    }

    /**
     * Set the locale of the request log.
     *
     * @param logLocale locale object
     */
    public void setLogLocale(final Locale logLocale) {
        this.logLocale = logLocale;
    }

    /**
     * Controls logging of the request hostname.
     *
     * @param logServer true - request hostname will be logged, false - request
     *                  hostname will not be logged
     */
    public void setLogServer(final boolean logServer) {
        this.logServer = logServer;
    }

    /**
     * Set the timezone of the request log.
     *
     * @param tz timezone string
     */
    public void setLogTimeZone(final String tz) {
        this.logTimeZone = tz;
    }

    /**
     * Controls whether the actual IP address of the connection or the IP address
     * from the X-Forwarded-For header will be logged.
     *
     * @param preferProxiedForAddress true - IP address from header will be logged,
     *                                false - IP address from the connection will be
     *                                logged
     */
    public void setPreferProxiedForAddress(final boolean preferProxiedForAddress) {
        this.preferProxiedForAddress = preferProxiedForAddress;
    }

    /**
     * Write requestEntry out. (to disk or slf4j log)
     *
     * @param requestEntry the request entry
     * @throws IOException if unable to write the entry
     */
    public void write(final String requestEntry) throws IOException {
        if (this.httpDebug) {
            logger.info(requestEntry);
        }
    }
}
