/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.safekeeping.cxf;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.crypto.NoSuchPaddingException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Endpoint;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http.spi.JettyHttpServer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.vmware.guest.appmonitor.VmGuestAppMonitor;
import com.vmware.guest.appmonitor.VmGuestAppMonitorException;
import com.vmware.jvix.JVixException;
import com.vmware.jvix.VddkVersion;
import com.vmware.safekeeping.common.BiosUuid;
import com.vmware.safekeeping.common.ConsoleWrapper;
import com.vmware.safekeeping.common.Utility;
import com.vmware.safekeeping.core.command.options.ExtensionManagerOptions;
import com.vmware.safekeeping.core.control.IoFunction;
import com.vmware.safekeeping.core.control.IoFunctionInterface;
import com.vmware.safekeeping.core.control.SafekeepingVersion;
import com.vmware.safekeeping.core.control.Vmbk;
import com.vmware.safekeeping.core.exception.SafekeepingException;
import com.vmware.safekeeping.core.profile.CoreGlobalSettings;
import com.vmware.safekeeping.core.soap.sso.SecurityUtil;
import com.vmware.safekeeping.core.type.VmbkThreadFactory;
import com.vmware.safekeeping.core.type.enums.ExtensionManagerOperation;
import com.vmware.safekeeping.cxf.support.JettyRequestLog;
import com.vmware.safekeeping.external.type.NoIoFunction;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Cxf {
    @WebServlet("/status")
    public static class BlockingServlet extends HttpServlet {
        private static final String XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<vimhealth xmlns=\"http://www.vmware.com/vi/healthservice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" schemaVersion=\"1.0\">"
                + "<health id=\"com.vmware.safekeeping\">" + "<name>VMware Safekeeping</name>"
                + "<status>green</status>" + "</health>" + "</vimhealth>";
        /**
         *
         */
        private static final long serialVersionUID = 5210820792431336463L;

        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException {
            response.setContentType("application/xml");
            response.setStatus(HttpServletResponse.SC_OK);
            try {
                response.getWriter().println(XML_STRING);
            } catch (final IOException e) {
                Utility.logWarning(logger, e);
            }
        }

        @Override
        protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException, IOException {
            // do nothing
        }

    }

    public static class BlockingServletAsync extends HttpServlet {

        /**
         *
         */
        private static final long serialVersionUID = -2706664543037254009L;
        private static final String XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<vimhealth xmlns=\"http://www.vmware.com/vi/healthservice\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" schemaVersion=\"1.0\">"
                + "<health id=\"com.vmware.safekeeping\">" + "<name>VMware Safekeeping</name>"
                + "<status>green</status>" + "</health>" + "</vimhealth>";

        @Override
        protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException {
            response.setContentType("application/xml");
            response.setStatus(HttpServletResponse.SC_OK);

            final ByteBuffer content = ByteBuffer.wrap(XML_STRING.getBytes(StandardCharsets.UTF_8));
            try {
                final AsyncContext async = request.startAsync();
                final ServletOutputStream out = response.getOutputStream();
                out.setWriteListener(new WriteListener() {
                    @Override
                    public void onError(final Throwable t) {
                        getServletContext().log("Async Error", t);
                        async.complete();
                    }

                    @Override
                    public void onWritePossible() throws IOException {
                        while (out.isReady()) {
                            if (!content.hasRemaining()) {
                                response.setStatus(HttpServletResponse.SC_OK);
                                async.complete();
                                return;
                            }
                            out.write(content.get());
                        }
                    }
                });
            } catch (IllegalStateException | IOException e) {
                Utility.logWarning(logger, e);

            }
        }

        @Override
        protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
                throws ServletException, IOException {
            // do nothing
        }

    }

    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(Cxf.class.getName());

    private static final int DEFAULT_NUMBER_OF_CONCURRENTS_VDDK_THREADS = 4;

    private static final int DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS = 10;

    private static final int DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS = 10;
    private static final int OUTPUT_BUFFER_SIZE = 32768;
    private static final int STRICT_TRANSPORT_SECURITY_MAX_AGE = 2000;
    private static final String QUIT_COMMAND = "quit";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_PORT = "port";
    private static final String OPTION_SECUREPORT = "securePort";
    private static final String OPTION_CONTEXT = "context";

    private static final String OPTION_BIND = "bind";
    private static final String OPTION_NO_HTTP = "noHttp";
    private static final String OPTION_NO_HTTPS = "noHttps";
    private static final String OPTION_DEBUG = "debug";
    private static final String OPTION_INTERACTIVE = "interactive";

    private static final String OPTION_NO_GUEST_MONITOR = "noGuestMonitor";
    private static final String OPTION_CONFIG = "config";

    private static final String OPTION_VDDK_LIST = "vddkList";

    private static final String OPTION_VDDK_VERSION = "vddkVersion";

    private static final String OPTION_VDDK_THREADS = "vddkThreads";

    private static final String OPTION_FCO_THREADS = "fcoThreads";

    private static final String OPTION_ARCHIVE_THREADS = "archiveThreads";

    private static final int APPLICATION_MONITOR_SCHEDULER_INITIAL_DELAY = 2;

    private static final int APPLICATION_MONITOR_SCHEDULER_PERIOD = 25;

    private static final String OPTION_JETTY_LOG = "jettyLog";

    private Server serverlet;

    private JettyHttpServer jettyServer;

    private SapiImpl sapi;

    private Endpoint ep;

    private boolean shutdownRequested;

    private boolean httpConnector;

    private boolean httpsConnector;
    private Integer plainPort;
    private Integer securePort;
    private String context;
    private File configFile;
    private boolean interactive;
    private boolean debug;
    private boolean jettyLog;
    private int numberOfConcurrentsFcoThreads = DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS;
    private int numberOfConcurrentsVddkThreads = DEFAULT_NUMBER_OF_CONCURRENTS_VDDK_THREADS;
    private int numberOfConcurrentsArchiveThreads = DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS;
    private String binding;
    private boolean enableHttps;
    private boolean enableHttp;
    private boolean guestMonitorEnabled;
    private VmGuestAppMonitor guestTools;
    private ScheduledExecutorService appMonitorScheduler;

    public Cxf() {
        this.httpsConnector = false;
        this.httpConnector = false;
        this.numberOfConcurrentsFcoThreads = DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS;
        this.numberOfConcurrentsVddkThreads = DEFAULT_NUMBER_OF_CONCURRENTS_VDDK_THREADS;
        this.numberOfConcurrentsArchiveThreads = DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS;
        this.shutdownRequested = false;
        this.plainPort = CxfGlobalSettings.getHttpPort();
        this.securePort = CxfGlobalSettings.getHttpsPort();
        this.context = CxfGlobalSettings.getContext();
        this.binding = CxfGlobalSettings.getBind();
        this.configFile = null;
        this.interactive = false;
        this.debug = false;
        jettyLog = false;
        this.enableHttps = CxfGlobalSettings.isHttpsEnabled();
        this.enableHttp = CxfGlobalSettings.isHttpEnabled();
        this.guestMonitorEnabled = CxfGlobalSettings.isGuestMonitorEnabled();
    }

    private void addDaemonShutdownHook(final Vmbk vmbk) {

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Cxf.this.shutdownRequested = true;
                shutdown();
                logger.info("Closing connection...");
                Cxf.this.sapi.close();
                vmbk.close();

            }
        });
    }

    private OptionParser configureParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(OPTION_HELP);
        parser.accepts(OPTION_PORT).withRequiredArg().ofType(Integer.class)
                .describedAs(String.format("Standard http port (%d default)", CxfGlobalSettings.getHttpPort()))
                .defaultsTo(CxfGlobalSettings.getHttpPort());
        parser.accepts(OPTION_SECUREPORT).withRequiredArg().ofType(Integer.class)
                .describedAs(String.format("Standard https port (%d default)", CxfGlobalSettings.getHttpsPort()))
                .defaultsTo(CxfGlobalSettings.getHttpsPort());
        parser.accepts(OPTION_CONTEXT).withRequiredArg().ofType(String.class)
                .describedAs(String.format("server context (%s default)", CxfGlobalSettings.getContext()))
                .defaultsTo(CxfGlobalSettings.getContext());
        parser.accepts(OPTION_BIND).withRequiredArg().ofType(String.class).describedAs(String
                .format("binding Ip address. 0.0.0.0 bind to all interfaces (%s default)", CxfGlobalSettings.getBind()))
                .defaultsTo(CxfGlobalSettings.getBind());
        parser.accepts(OPTION_NO_HTTP, "No Http binding").availableUnless(OPTION_PORT);
        parser.accepts(OPTION_NO_HTTPS, "No Https binding").availableUnless(OPTION_SECUREPORT);
        parser.accepts(OPTION_DEBUG, "Debug mode");
        parser.accepts(OPTION_JETTY_LOG, "Jetty Http logs");
        parser.accepts(OPTION_INTERACTIVE, "Interactive mode");
        parser.accepts(OPTION_NO_GUEST_MONITOR, "Disable VMware Guest Application Monitor");
        parser.accepts(OPTION_CONFIG).withRequiredArg().ofType(File.class).describedAs("configuration file");
        parser.accepts(OPTION_VDDK_THREADS).withRequiredArg().ofType(Integer.class)
                .describedAs("Number of parallel VDDK operations")
                .defaultsTo(DEFAULT_NUMBER_OF_CONCURRENTS_VDDK_THREADS);
        parser.accepts(OPTION_FCO_THREADS).withRequiredArg().ofType(Integer.class)
                .describedAs("Number of parallel backup restore operations ")
                .defaultsTo(DEFAULT_NUMBER_OF_CONCURRENTS_FCO_THREADS);
        parser.accepts(OPTION_ARCHIVE_THREADS).withRequiredArg().ofType(Integer.class)
                .describedAs("Number of parallel archive operations ")
                .defaultsTo(DEFAULT_NUMBER_OF_CONCURRENTS_ARCHIVE_THREADS);
        parser.accepts(OPTION_VDDK_VERSION).withRequiredArg().ofType(String.class)
                .describedAs("Vddk Version to use (ex. 7.0.1.16860560");
        parser.accepts(OPTION_VDDK_LIST, "Show list of available Vddk Versions");
        return parser;

    }

    private void create(final Vmbk vmbk, final boolean debug)
            throws IOException, CertificateEncodingException, NoSuchAlgorithmException {

        this.serverlet = createServer();

        serverlet.setRequestLog(new JettyRequestLog(jettyLog));
        this.jettyServer = new JettyHttpServer(this.serverlet, true);
        this.sapi = new SapiImpl(vmbk, debug, generateExtensionOptionsClass());
        this.ep = Endpoint.create(this.sapi);

        final ContextHandlerCollection collection1 = new ContextHandlerCollection();
        ServletContextHandler handler = new ServletContextHandler(serverlet, "/");
        handler.addServlet(BlockingServletAsync.class, "/status");
        collection1.addHandler(handler);
        this.serverlet.setHandler(collection1);

        this.ep.publish(this.jettyServer.createContext(this.context));
    }

    private Server createServer() {

        final List<Connector> connectors = new ArrayList<>();
        // Create a basic jetty server object without declaring the port. Since
        // we are configuring connectors directly we'll be setting ports on
        // those connectors.
        final Server server = new Server();
        // HTTP Configuration
        // HttpConfiguration is a collection of configuration information
        // appropriate for http and https. The default scheme for http is
        // <code>http</code> of course, as the default for secured http is
        // <code>https</code> but we show setting the scheme to show it can be
        // done. The port for secured communication is also set here.
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setOutputBufferSize(OUTPUT_BUFFER_SIZE);
        if (this.enableHttp) {
            // HTTP connector
            // The first server connector we create is the one for http, passing
            // in
            // the http configuration we configured above so it can get things
            // like
            // the output buffer size, etc. We also set the port (8080) and
            // configure an idle timeout.
            final ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
            http.setHost(this.binding);
            http.setPort(this.plainPort);
            http.setIdleTimeout(Utility.THIRTY_SECONDS_IN_MILLIS);
            this.httpConnector = connectors.add(http);
        }

        if (this.enableHttps) {
            final Path keystorePath = Paths.get(CoreGlobalSettings.getKeystorePath());
            if (keystorePath.toFile().exists()) {
                // HTTP Configuration
                // HttpConfiguration is a collection of configuration
                // information
                // appropriate for http and https. The default scheme for http
                // is
                // <code>http</code> of course, as the default for secured http
                // is
                // <code>https</code> but we show setting the scheme to show it
                // can be
                // done. The port for secured communication is also set here.

                httpConfig.setSecureScheme("https");
                httpConfig.setSecurePort(this.securePort);

                // SSL Context Factory for HTTPS
                // SSL requires a certificate so we configure a factory for ssl
                // contents
                // with information pointing to what keystore the ssl connection
                // needs
                // to know about. Much more configuration is available the ssl
                // context,
                // including things like choosing the particular certificate out
                // of a
                // keystore to be used.

                final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
                sslContextFactory.setKeyStorePath(keystorePath.toString());
                sslContextFactory.setKeyStorePassword(CoreGlobalSettings.getKeyStorePassword());
                sslContextFactory.setKeyManagerPassword(CoreGlobalSettings.getKeyStoreSslCertificatePassword());
                sslContextFactory.setCertAlias(CoreGlobalSettings.getKeyStoreSslAlias());
                final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory,
                        HttpVersion.HTTP_1_1.asString());

                // OPTIONAL: Un-comment the following to use Conscrypt for SSL
                // instead of
                // the native JSSE implementation.

                /**
                 * Security.addProvider(new OpenSSLProvider());
                 * sslContextFactory.setProvider("Conscrypt");
                 */

                /**
                 * HTTPS Configuration A new HttpConfiguration object is needed for the next
                 * connector and you can pass the old one as an argument to effectively clone
                 * the contents. On this HttpConfiguration object we add a
                 * SecureRequestCustomizer which is how a new connector is able to resolve the
                 * https connection before handing control over to the Jetty Server.
                 */

                final HttpConfiguration httpsConfig = new HttpConfiguration(httpConfig);
                final SecureRequestCustomizer src = new SecureRequestCustomizer();
                src.setStsMaxAge(STRICT_TRANSPORT_SECURITY_MAX_AGE);
                src.setStsIncludeSubDomains(true);
                httpsConfig.addCustomizer(src);

                // HTTPS connector
                // We create a second ServerConnector, passing in the http
                // configuration
                // we just made along with the previously created ssl context
                // factory.
                // Next we set the port and a longer idle timeout.
                final ServerConnector https = new ServerConnector(server, sslConnectionFactory,
                        new HttpConnectionFactory(httpsConfig));
                https.setHost(this.binding);
                https.setPort(this.securePort);
                https.setIdleTimeout(Utility.TEN_MINUTES_IN_MILLIS);
                this.httpsConnector = connectors.add(https);
            } else {
                ConsoleWrapper.console.println(keystorePath.toString() + " doesn't exist");
            }
        }
        // Here you see the server having multiple connectors registered with
        // it, now requests can flow into the server from both http and https
        // urls to their respective ports and be processed accordingly by jetty.
        // A simple handler is also registered with the server so the example
        // has something to pass requests off to.

        // Set the connectors
        server.setConnectors(connectors.toArray(new ServerConnector[0]));

        return server;
    }

    private void daemonize(final Vmbk vmbk) {
        System.out.close();
        System.err.close();
        addDaemonShutdownHook(vmbk);
    }

    private ExtensionManagerOptions generateExtensionOptionsClass()
            throws NoSuchAlgorithmException, CertificateEncodingException {
        final ExtensionManagerOptions extOpt = new ExtensionManagerOptions(ExtensionManagerOperation.NONE);
        final SecurityUtil aesCert = SecurityUtil.loadFromKeystore(CoreGlobalSettings.getKeystorePath(),
                CoreGlobalSettings.getKeyStorePassword(), CoreGlobalSettings.getKeyStoreSslAlias());
        final BiosUuid bios = BiosUuid.getInstance();
        final String fqdn = bios.getiIpAddress();
        extOpt.setHealthInfoUrl(String.format("https://%s:%d/status", fqdn, this.securePort));
        extOpt.setServerInfoUrl(String.format("https://%s:%d%s", fqdn, this.securePort, this.context));

        final MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        sha1.update(aesCert.getUserCert().getEncoded());
        final String sha1Digest = Utility.toHexString(sha1.digest()).replace(" ", ":");
        extOpt.setServerThumbprint(sha1Digest.substring(0, sha1Digest.length() - 1));
        return extOpt;
    }

    private void initAppMon() throws VmGuestAppMonitorException {

        this.guestTools = VmGuestAppMonitor.getInstance();
        this.guestTools.enable();
        final Runnable keepMonitorAliveRunnable = () -> {
            switch (this.guestTools.getAppStatus()) {
            case GREEN:
                this.guestTools.markActive();
                break;
            case GRAY:
            case RED:
                this.guestTools.enable();
                this.guestTools.markActive();
                break;
            }
        };
        this.appMonitorScheduler = Executors
                .newSingleThreadScheduledExecutor(new VmbkThreadFactory("AppMonitor", true));
        this.appMonitorScheduler.scheduleAtFixedRate(keepMonitorAliveRunnable,
                APPLICATION_MONITOR_SCHEDULER_INITIAL_DELAY, APPLICATION_MONITOR_SCHEDULER_PERIOD, TimeUnit.SECONDS);
    }

    /**
     * Initialize Safekeeping
     *
     * @param configFile
     * @param ioManager
     * @param numberOfConcurrentsFcoThreads
     * @param numberOfVddkThreads
     * @param numberOfConcurrentsArchiveThreads
     * @return
     * @throws SafekeepingException
     */
    private Vmbk initialize(final File configFile, final IoFunctionInterface ioManager,
            final int numberOfConcurrentsFcoThreads, final int numberOfVddkThreads,
            final int numberOfConcurrentsArchiveThreads) throws SafekeepingException {
        final Vmbk vmbk = new Vmbk();
        try {
            IoFunction.setFunction(ioManager);

            String fileName;
            if (configFile == null) {
                fileName = CoreGlobalSettings.getDefaulConfigPropertiesFile();
            } else {
                fileName = configFile.getPath();
            }
            final Boolean configReturnStatus = vmbk.configure(fileName);
            if (Boolean.FALSE.equals(configReturnStatus)) {
                ConsoleWrapper.console
                        .println("There is a problem with the configuration. Manual reconfiguration enforced");

            } else {
                vmbk.initialize(numberOfConcurrentsFcoThreads, numberOfVddkThreads, numberOfConcurrentsArchiveThreads);
            }
        } catch (final IOException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
                | NoSuchPaddingException | JVixException | SafekeepingException | URISyntaxException e) {
            Utility.logWarning(logger, e);
            ConsoleWrapper.console.println("Setting initialiation failed.");
            throw new SafekeepingException(e);
        }
        return vmbk;
    }

    /**
     * instance in interactive mode
     * 
     * @param vmbk
     * @throws Exception
     */
    private void interactive(final Vmbk vmbk) throws Exception {
        final BiosUuid bios = BiosUuid.getInstance();
        ConsoleWrapper.console.println(SafekeepingVersion.getInstance().getExtendedVersion());
        ConsoleWrapper.console.println(SafekeepingVersion.getInstance().getJavaRuntime());
        ConsoleWrapper.console.println(bios.getExtendedVersion());

        final StringBuilder endpoint = new StringBuilder();
        endpoint.append("GuestAppMonitoring: ");
        endpoint.append((this.guestTools.isEnabled()) ? "Enabled" : "Disabled");
        endpoint.append("%nHealth Info:%n");
        if (this.httpConnector) {
            endpoint.append(String.format("\t\thttp://%s:%d/status%n", bios.getHostname(), this.plainPort));
        }
        if (this.httpsConnector) {
            endpoint.append(String.format("\t\thttps://%s:%d/status%n", bios.getiIpAddress(), this.securePort));
        }
        endpoint.append("%nEndpoint(s): ");

        endpoint.append((this.sapi.isDebugMode()) ? "(Debug)%n" : "%n");
        if (this.httpConnector) {
            endpoint.append(String.format("\t\thttp://%s:%d%s%n", bios.getHostname(), this.plainPort, this.context));
            endpoint.append(String.format("\t\thttp://%s:%d%s%n", bios.getiIpAddress(), this.plainPort, this.context));
        }
        if (this.httpsConnector) {
            endpoint.append(String.format("\t\thttps://%s:%d%s%n", bios.getHostname(), this.securePort, this.context));
            endpoint.append(
                    String.format("\t\thttps://%s:%d%s%n", bios.getiIpAddress(), this.securePort, this.context));
        }
        this.serverlet.start();
        ConsoleWrapper.console.println(endpoint.toString());
        try (final Scanner scan = new Scanner(System.in)) {
            ConsoleWrapper.console.printf("quit to exit . . . ");
            while (true) {
                final String st = scan.nextLine();
                if (st.equalsIgnoreCase(QUIT_COMMAND)) {
                    break;
                }
            }
        }
        ConsoleWrapper.console.println();
        ConsoleWrapper.console.println("Shutting WebService...");
        shutdown();
        ConsoleWrapper.console.println("Closing connection...");
        this.sapi.close();
        ConsoleWrapper.console.println("Cleaning Temporary files...");
        vmbk.close();

        Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
        ConsoleWrapper.console.println("Bye. ");
    }

    public boolean isShutdownRequested() {
        return this.shutdownRequested;
    }

    public boolean parse(final String[] args) throws IOException {
        final OptionParser parser = configureParser();

        final OptionSet options = parser.parse(args);
        if (options.has(OPTION_HELP)) {
            parser.printHelpOn(ConsoleWrapper.console.getWriter());
            return false;
        }
        if (options.has(OPTION_VDDK_LIST)) {
            try {
                SafekeepingVersion.initialize(null);
                final List<VddkVersion> versions = SafekeepingVersion.getInstance().getVddkVersionsList();
                for (final VddkVersion ver : versions) {
                    ConsoleWrapper.console.println(ver.getExtendedVersion());
                }
            } catch (JVixException | IOException | SafekeepingException | URISyntaxException e) {
                Utility.logWarning(logger, e);
            }
            return false;
        }
        if (options.has(OPTION_VDDK_THREADS)) {
            this.numberOfConcurrentsVddkThreads = (int) options.valueOf(OPTION_VDDK_THREADS);
        }
        if (options.has(OPTION_FCO_THREADS)) {
            this.numberOfConcurrentsFcoThreads = (int) options.valueOf(OPTION_FCO_THREADS);
        }
        if (options.has(OPTION_ARCHIVE_THREADS)) {
            this.numberOfConcurrentsArchiveThreads = (int) options.valueOf(OPTION_ARCHIVE_THREADS);
        }
        if (options.has(OPTION_VDDK_VERSION)) {
            final String vddkVersion = options.valueOf(OPTION_VDDK_VERSION).toString();
            try {
                SafekeepingVersion.initialize(vddkVersion);
            } catch (JVixException | IOException | SafekeepingException | URISyntaxException e) {
                Utility.logWarning(logger, e);
            }
        }

        if (options.has(OPTION_PORT)) {
            this.plainPort = (int) options.valueOf(OPTION_PORT);
            this.enableHttp = true;
        }
        if (options.has(OPTION_SECUREPORT)) {
            this.securePort = (int) options.valueOf(OPTION_SECUREPORT);
            this.enableHttps = true;
        }
        if (options.has(OPTION_CONTEXT)) {

            this.context = options.valueOf(OPTION_CONTEXT).toString();
        }
        if (options.has(OPTION_BIND)) {
            this.binding = options.valueOf(OPTION_BIND).toString();
        }
        if (options.has(OPTION_NO_HTTP)) {
            this.enableHttp = false;
        }
        if (options.has(OPTION_NO_HTTPS)) {
            this.enableHttps = false;
        }
        if (options.has(OPTION_NO_GUEST_MONITOR)) {
            this.guestMonitorEnabled = false;
        }

        if (options.has(OPTION_CONFIG)) {
            this.configFile = (File) options.valueOf(OPTION_CONFIG);
        }
        this.debug = options.has(OPTION_DEBUG);
        this.interactive = options.has(OPTION_INTERACTIVE);
        this.jettyLog = options.has(OPTION_JETTY_LOG);
        return true;
    }

    public void run() throws Exception {
        final Vmbk vmbk = initialize(this.configFile, new NoIoFunction(), this.numberOfConcurrentsFcoThreads,
                this.numberOfConcurrentsVddkThreads, this.numberOfConcurrentsArchiveThreads);
        create(vmbk, this.debug);
        if (this.guestMonitorEnabled) {
            initAppMon();
        }
        if (this.interactive) {
            interactive(vmbk);
        } else {
            daemonize(vmbk);
            this.serverlet.start();
            while (!isShutdownRequested()) {
                Thread.sleep(Utility.ONE_SECOND_IN_MILLIS);
            }
        }

    }

    private void shutdown() {
        if (this.guestMonitorEnabled && (this.appMonitorScheduler != null)) {
            if (this.guestTools.isEnabled()) {
                this.guestTools.disable();
            }
            this.appMonitorScheduler.shutdown();
        }
        if (this.ep != null) {
            logger.info("Shutting down EndPoint...");
            this.ep.stop();
        }
        if (this.jettyServer != null) {
            logger.info("Shutting down Jetty ...");
            this.jettyServer.stop(Utility.FIVE_SECONDS);
        }
        if (this.serverlet != null) {
            try {
                logger.info("Shutting down servlet ...");
                this.serverlet.stop();
            } catch (final Exception e) {
                Utility.logWarning(logger, e);
            }
        }

        logger.info("WebService is down");
    }

}
