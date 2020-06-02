[![Build status](https://github.com/fcrespel/runtastic-export-converter/workflows/CI/badge.svg)](https://github.com/fcrespel/runtastic-export-converter/actions?query=workflow%3ACI)
[![License](https://img.shields.io/github/license/fcrespel/runtastic-export-converter.svg)](https://opensource.org/licenses/MIT)

# Runtastic Export Converter

This project provides a command-line tool to convert Runtastic JSON exported data to standard GPX/TCX files.

It was created after Runtastic closed its web site and removed the ability to export individual sport sessions as GPX/TCX files in August 2019.
The only remaining possibility for users to get their data is with a GDPR-compliant "full export" archive containing mostly JSON files, that cannot be imported as-is into other applications.
This project thus aims to provide a way to convert Runtastic data to standard formats recognized by other services.

## Building

As a prerequisite, make sure you have OpenJDK installed (e.g. [AdoptOpenJDK](https://adoptopenjdk.net)).

To build the project locally, execute the following command, from the project directory:

    ./mvnw clean package

## Running

First, make sure to unzip the Runtastic export archive in a directory of your choice and note its path.

To run the command line tool, use the `jar-with-dependencies` JAR file in the `target` directory, for example:

    java -jar runtastic-export-converter-jar-with-dependencies.jar
    
    Expected arguments:
      check    <export path>
      list     <export path> <filter>
      user     <export path>
      info     <export path> <activity id>
      photo    <export path> <photo id>
      convert  <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']
      overlap  <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']
      compound <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']
      help


To check an export (here, `runtastic-export-20190807-000` in the current directory):

    java -jar runtastic-export-converter-jar-with-dependencies.jar list runtastic-export-20190807-000

To list sport sessions from an export directory (here, `runtastic-export-20190807-000` in the current directory):

    java -jar runtastic-export-converter-jar-with-dependencies.jar list runtastic-export-20190807-000

To list sport sessions from an export directory, for a "specific" filter, add the filter (keyword) as 3rd option. The tool searches within session description, session id, photo id, photo description and equipment id for the filter (keyword). Here an example searching for 'Marathon':

    java -jar runtastic-export-converter-jar-with-dependencies.jar list runtastic-export-20190807-000 Marathon


To display user information from an export directory:

    java -jar runtastic-export-converter-jar-with-dependencies.jar user runtastic-export-20190807-000

To display detail information of a sport session (here, session ID `fdd2f131-ef65-4e6c-b27e-50b8ecf465d4` and `runtastic-export-20190807-000` in the current directory):

    java -jar runtastic-export-converter-jar-with-dependencies.jar info runtastic-export-20190807-000 fdd2f131-ef65-4e6c-b27e-50b8ecf465d4

To get a sport session for a dedicated photo (here, 5097940.jpg):

    java -jar runtastic-export-converter-jar-with-dependencies.jar photo runtastic-export-20190807-000 5097940


To convert a single sport session to TCX (here, session ID `fdd2f131-ef65-4e6c-b27e-50b8ecf465d4`):

    java -jar runtastic-export-converter-jar-with-dependencies.jar convert runtastic-export-20190807-000 fdd2f131-ef65-4e6c-b27e-50b8ecf465d4 fdd2f131-ef65-4e6c-b27e-50b8ecf465d4.tcx

To convert all sport sessions to GPX (here, in a `runtastic-export-gpx` directory):

    java -jar runtastic-export-converter-jar-with-dependencies.jar convert runtastic-export-20190807-000 all runtastic-export-gpx gpx


To calculate overlapping sport sessions for all sessions:

    java -jar runtastic-export-converter-jar-with-dependencies.jar overlap runtastic-export-20190807-000 

The command above analyzes all sport sessions and searches for 'overlapping' sport session bounds by comparing the bounds of each sport session. 


To calculate overlapping sport sessions for all sessions and export them:

    java -jar runtastic-export-converter-jar-with-dependencies.jar overlap runtastic-export-20190807-000 runtastic-export-gpx gpx

The command above analyzes all sport sessions and searches for 'overlapping' sport session bounds by comparing the bounds of each sport session.
It further exports them into destination path. The exported sport session contains all overlapping sport sessions. 


To calculate compound sport sessions for all sessions:

    java -jar runtastic-export-converter-jar-with-dependencies.jar compound runtastic-export-20190807-000 

The command above analyzes all sport sessions and searches for 'compound' sport sessions by comparing the bounds of each sport session. 


## License

This project is licensed under the open-source [MIT License](https://opensource.org/licenses/MIT).

It is provided "as is" with no warranty of any kind, express or implied. Please refer to the license terms for more information.
