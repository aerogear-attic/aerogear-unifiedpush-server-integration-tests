package org.arquillian.spacelift.gradle

import org.arquillian.spacelift.Spacelift
import org.arquillian.spacelift.task.Task
import org.arquillian.spacelift.task.os.CommandTool

class CertificateGenerator extends Task<Object, Void> {

    private String commonName = 'localhost'
    private String alias = 'aerogear'
    private String password = 'aerogear'
    private File trustStore
    private File apnsCertificate
    private File gcmCertificate

    String commonName() {
        return commonName
    }

    CertificateGenerator commonName(String commonName) {
        this.commonName = commonName
        return this
    }

    String alias() {
        return alias
    }

    CertificateGenerator alias(String alias) {
        this.alias = alias
        return this
    }

    String password() {
        return password
    }

    CertificateGenerator password(String password) {
        this.password = password
        return this
    }

    File trustStore() {
        return trustStore
    }

    CertificateGenerator trustStore(String trustStorePath) {
        return trustStore(new File(trustStorePath))
    }

    CertificateGenerator trustStore(File trustStore) {
        this.trustStore = trustStore
        return this
    }

    File apnsCertificate() {
        return apnsCertificate
    }

    CertificateGenerator apnsCertificate(String apnsCertificatePath) {
        return apnsCertificate(new File(apnsCertificatePath))
    }

    CertificateGenerator apnsCertificate(File apnsCertificate) {
        this.apnsCertificate = apnsCertificate
        return this
    }

    File gcmCertificate() {
        return gcmCertificate
    }

    CertificateGenerator gcmCertificate(String gcmCertificatePath) {
        return gcmCertificate(new File(gcmCertificatePath))
    }

    CertificateGenerator gcmCertificate(File gcmCertificate) {
        this.gcmCertificate = gcmCertificate
        return this
    }


    @Override
    protected Void process(Object input) throws Exception {
        createApnsCertificate()

        importCertificatesIntoTrustStore()

        return null
    }

    private void createApnsCertificate() {
        CommandTool keytool = Spacelift.task('keytool') as CommandTool

        keytool.parameters('-genkey', '-noprompt')
                .parameters('-alias', alias)
                .parameters('-dname', "CN=$commonName, OU=UnifiedPush, O=AeroGear, C=US")
                .parameters('-ext', "san=ip:$commonName")
                .parameters('-keystore', apnsCertificate.absolutePath)
                .parameters('-storepass', password)
                .parameters('-keypass', password)
                .parameters('-validity', '365')
                .parameters('-keyalg', 'RSA')
                .parameters('-keysize', '2048')
//                .parameters('-storetype', 'pkcs12')

        keytool.execute().await()
    }

    private void importCertificatesIntoTrustStore() {
        CommandTool keytool = Spacelift.task('keytool') as CommandTool

        keytool.parameters('-export', '-noprompt')
                .parameters('-alias', alias)
                .parameters('-keystore', apnsCertificate.absolutePath)
                .parameters('-storepass', password)
                .parameters('-rfc', '-file', "${apnsCertificate.absolutePath}.cer")
                .execute().await()

        keytool = Spacelift.task('keytool') as CommandTool

        keytool.parameters('-import', '-noprompt')
                .parameters('-alias', 'apns')
                .parameters('-file', "${apnsCertificate.absolutePath}.cer")
                .parameters('-storepass', password)
                .parameters('-keystore', trustStore.absolutePath)
                .execute().await()

        keytool = Spacelift.task('keytool') as CommandTool

        keytool.parameters('-import', '-noprompt')
                .parameters('-alias', 'gcm')
                .parameters('-file', gcmCertificate.absolutePath)
                .parameters('-storepass', password)
                .parameters('-keystore', trustStore.absolutePath)
                .execute().await()

        /*keytool.parameters('-importkeystore')
//                .parameters('-v', '-trustcacerts')
                .parameters('-alias', alias)
                .parameters('-srckeystore', apnsCertificate.absolutePath)
                .parameters('-srcstorepass', password)
                .parameters('-srcstoretype', 'pkcs12')
                .parameters('-destkeystore', trustStore.absolutePath)
                .parameters('-deststoretype', 'jks')
                .parameters('-storepass', password)
                .parameters('-keypass', password)

        keytool.execute().await()*/

//        keytool -importkeystore -destkeystore mykeystore.jks -srckeystore keystore.p12 -srcstoretype pkcs12 -alias myservercert


        /*
        keytool -import -storepass "Be Your Own Lantern" -keystore /home/asaleh/Work/PROXY/JBOSS/jboss-eap-6.3/bin/littleproxy_keystore.jks -alias android -file /home/asaleh/Work/jbossqe-mobile/patches/certs/sslcerts/myHost.crt  -trustcacerts -keypass aerogear

keytool -import -storepass aerogear -keystore /home/asaleh/Work/jbossqe-mobile/patches/certs/aerogear.keystore -alias android -file /home/asaleh/Work/jbossqe-mobile/patches/certs/sslcerts/myHost.crt -noprompt -trustcacerts -keypass aerogear

keytool -import -v -trustcacerts -alias android -file /home/asaleh/Work/jbossqe-mobile/patches/certs/sslcerts/myHost.crt -keystore /home/asaleh/Work/jbossqe-mobile/patches/certs/aerogear.truststore -keypass aerogear -storepass aerogear
         */
    }

}