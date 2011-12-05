NOAA Weather Station Viewer
---------------------------

A simple desktop application displaying the current weather 
conditions at each of the NOAA stations. 

#### Artifact

A pure Java library, with main class 
`org.hawkinssoftware.ui.util.weather.WeatherViewerMain`

#### Installation

At present, the Weather Viewer can only be run from Eclipse.

* Import the weather-app project into Eclipse using 
  **File > Import... > Maven Projects**
* Create an Eclipse run profile for class 
  **WeatherViewerMain** with VM args:
    + -javaagent:target/rns-agent.jar 
    + -Djava.library.path=target/native 
    + -Dsun.awt.noerasebackground=true 
    + -Dsun.java2d.noddraw=true 
    + -Dsun.java2d.d3d=false

#### Features

Viewing current weather conditions is simply a matter of selecting 
a region in the upper-left list, and then selecting a station in 
the lower-left list. Station data is updated roughly once per 
hour, and will typically be unavailable for a few stations.
        

        