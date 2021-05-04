package com.vmware.safekeeping.core.soap;

public class SsoX509CertFile {

    public String getX509CertFile() {
        return x509CertFile;
    }

    public String getX509CertPrivateKeyFile() {
        return x509CertPrivateKeyFile;
    }

    public Long getTicketLifeExpectancyInMilliSeconds() {
        return ticketLifeExpectancyInMilliSeconds;
    }

    public SsoX509CertFile(String x509CertFile, String x509CertPrivateKeyFile,
            Long ticketLifeExpectancyInMilliSeconds) {
        super();
        this.x509CertFile = x509CertFile;
        this.x509CertPrivateKeyFile = x509CertPrivateKeyFile;
        this.ticketLifeExpectancyInMilliSeconds = ticketLifeExpectancyInMilliSeconds;
    }

    private final String x509CertFile;

    private final String x509CertPrivateKeyFile;
    private final Long ticketLifeExpectancyInMilliSeconds;
}