/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jboss.as.cli.scriptsupport.*

cliBuilder = new CliBuilder(usage: 'groovy -cp "./jboss-cli-client.jar" jboss-as-setup.groovy')

cliBuilder.with {
    h(longOpt: 'help', 'Help - Usage Information')  
    m(argName:'managementHost', args: 1, longOpt: 'managementHost','management host for establishing connection')
    p(argName:'managementPort', args: 1, longOpt: 'managementPort','management port for establishing connection')
    t(argName:'httpPort', args: 1, longOpt: 'httpPort','setup the HTTP port')
    s(argName:'httpsPort', args: 1, longOpt: 'httpsPort','setup the HTTPS port')
    n(argName:'keyName', args: 1, longOpt: 'keyStoreName','setup the ssl element name')
    a(argName:'keyAlias', args: 1, longOpt: 'keyStoreAlias','setup the key alias')
    f(argName:'keyFile', args: 1, longOpt: 'keyStoreFile','setup the keystore file')
    w(argName:'keyPassphrase', args: 1, longOpt: 'keyStorePassphrase','setup the key password')
    l(argName:'sslProtocol', args: 1, longOpt: 'SSL protocol','setup the SSL protocol')
}

options = cliBuilder.parse(args)

if (options == null)
{
    System.exit(1)
}
else if ((options.t && !"${options.t}".isNumber()) || (options.s && !"${options.s}".isNumber()))
{
    println("Invalid Ports: " + (options.t ? "HTTP Port: '${options.t}'" : "") + (options.s ? " HTTPS Port: '${options.s}'" : ""))
    System.exit(1)
}
else if (options.h) 
{
    cliBuilder.usage()
    System.exit(0)
}
else
{
    if (options.t || options.s || (options.n && options.f && options.a && options.w && options.l))
    {

        managementHost = options.m ? "${options.m}" : '127.0.0.1'
        managementPort = options.p ? "${options.p}" : '9999'
        httpPort = "${options.t}"
        httpsPort = "${options.s}"
        keyName = "${options.n}"
        keyFile = "${options.f}"
        keyAlias = "${options.a}"
        keyPassword = "${options.w}"
        keyProtocol = "${options.l}"

        cli = CLI.newInstance()
        exception = null
        try {
            cli.connect(managementHost + ":" + managementPort)

            if (options.t)
            {
                cli.cmd("cd socket-binding-group=standard-sockets/socket-binding=http")
                println("Changing HTTP port:")
                result = cli.cmd(":write-attribute(name=port,value=" + httpPort + ")")
                cli.cmd("cd ../..")
                response = result.getResponse()
                println(response)
            }
    
            if (options.s)
            {
                cli.cmd("cd socket-binding-group=standard-sockets/socket-binding=https")
                println("Changing HTTPS port:")
                result = cli.cmd(":write-attribute(name=port,value=" + httpsPort + ")")
                cli.cmd("cd ../..")
                response = result.getResponse()
                println(response)
            }   

            if (options.n && options.f && options.a && options.w && options.l)
            {
                println("Checking if HTTPS connector already exists:")
                cli.cmd("cd subsystem=web")
                result = cli.cmd(":read-children-names(child-type=connector)")
                response = result.getResponse()
                println(response)
                if (response.asString().contains("https"))
                {
                    println("Removing existing HTTPS connector:")
                    result = cli.cmd("./connector=https:remove")
                    response = result.getResponse()
                    println(response)
                }
                println("Response from creating https connector:")
                result = cli.cmd("./connector=https:add(name=\"https\", protocol=\"HTTP/1.1\", scheme=\"https\", socket-binding=\"https\")")
                response = result.getResponse()
                println(response)
         
                println("Response from creating ssl element:")
                cli.cmd("cd connector=https")
                result = cli.cmd("./ssl=configuration:add(name=\"" + keyName + "\", key-alias=\"" + keyAlias + "\", password=\"" + keyPassword + "\", certificate-key-file=\"" + keyFile + "\", protocol=\"" + keyProtocol + "\")")
                response = result.getResponse()
                println(response)

                cli.cmd("cd ../..")
            }
 
            reloadCmd = ":reload"

            result = cli.cmd(reloadCmd)
            response = result.getResponse()
            println(response)
 
        } catch (Exception ex) {
            println(ex)
            exception = ex
        } finally { 
            try { cli.disconnect() } catch (Exception ignore) {}
            System.exit(exception == null ? 0 : 1)        
        }
    }
    else
    {
        System.exit(0)
    }   
}

