/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.common


class ServerSocketUtils {

    def createServerSocket(int port) {
        return new ServerSocket(port, 0, InetAddress.getByName("localhost"))
    }

    def readUntilMessageIsShown(ServerSocket providerSocket, String msg) {

        Socket connection = null
        BufferedReader input = null
        StringBuffer response = new StringBuffer()
        try{
            connection = providerSocket.accept()
            connection.setSoTimeout(2000)
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()))

            int result
            while ((result = input.read()) != -1) {
                response.append(Character.toChars(result))

                if (response.toString().contains(msg)) {
                    break
                }
            }
        }
        catch(Exception ex){
            //ex.printStackTrace();
        }
        finally{
            try{
                input.close()
            }
            catch(Exception e){
                e.printStackTrace()
            }
            try{
                providerSocket.close()
            }
            catch(Exception e){
                e.printStackTrace()
            }
        }
        return response.toString()
    }
}
