/*
 *   Copyright 2003-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 
#if !defined(__AXIS_AXISCONFIG_H_OF_AXIS_INCLUDED__)
#define __AXIS_AXISCONFIG_H_OF_AXIS_INCLUDED__

#define CONFBUFFSIZE 256

/**
 *   @class AxisConfig
 *   @brief class for taking configuration parameters.
 *   
 *   The purpose of this class is to provide a way so that
 *   the server takes the configuration path where the
 *   axiscpp.conf lies from the environment variable AXIS_HOME.
 *   User has to set this variable pointing the path to axiscpp.conf file.
 *   This axiscpp.conf file contains all the configuration paths
 *   like wsdd configuration file path, axis log path etc.
 *
 *   @author Damitha Kumarage (damitha@opensource.lk, damitha@jkcsworld.com)
 */

class AxisConfig
{
    public:
    AxisConfig();
    ~AxisConfig();

    /**
     * This is called to get the server.wsdd file path.
     * 
     * @return the server.wsdd file path 
     */
    char* getWsddFilePath();

    /**
     * This is called to get the AxisLog file path.
     *
     * @return the AxisLog file path.
     */
    char* getAxisLogPath();

    /**
     * This is called to get the AxisClientLog file path.
     *
     * @return the AxisClientLog file path.
     */
    char* AxisConfig::getAxisClientLogPath ();    

    /**
     * This is called to get the client.wsdd file path.
     * 
     * @return the client.wsdd file path 
     */
    char* getClientWsddFilePath();

    /**
     * This method will read from the configuration file called axiscpp.conf
     * which is located in a place defined by AXIS_HOME environment variable. 
     *
     * @return The status which indicates whether the operation is success 
     * (AXIS_SUCCESS) or not (AXIS_FAIL).
     */
    int readConfFile();

    /**
     * This is called to get the value of $AXIS_HOME env variable.
     * 
     * @return the $AXIS_HOME value
     */
    char* getAxisHomePath();
        
    private:         
        char* m_pcAxisHome;
        char* m_pcWsddFilePath;
        char* m_pcAxisLogPath;
        char* m_pcAxisClientLogPath;
        char* m_pcClientWsddFilePath;
};
#endif

