# Jobs plugin change log summary

## 2.5.6
* Added a multiplier to restricted areas
* Fixed memory leak with creatures from monster spawners

## 2.5.5
* Changed kill payouts to be based on creature spawn type, not distance from spawners
* Fixed duplicate honorific on /jobs reload
* Improved database performance
* Commands should work for offline players properly

## 2.5.4
* Yet another crafting NPE fix

## 2.5.3
* Fixed NPE when crafting
* Reload should actually work properly now

## 2.5.2
* Fixed some /jobs reload Issues
* Fix killing of creatures/animals for 1.8+

## 2.5.1
* Fixed NPE when crafting

## 2.5.0
* Added support for Crafting (via Spout)
* Added ability to broadcast on level up
* Fixed garbage collection error with MySQL and H2 when joining a job.

## 2.4.6
* Added iConomy 6 support
* Update for Bukkit build 1060

## 2.4.5
* Added Essentials Economy support
* Added configuration option "economy" for servers with with multiple economy plugins 

## 2.4.4
* Changed the method of fishing to use onPlayerFish instead of onPlayerPickupItem

## 2.4.3
* Jobs does a better job disabling itself

## 2.4.2
* Added /jobs reload command

## 2.4.1
* Fixed experience equation for fishing

## 2.4.0
* Changed experience handling to only display experience as whole numbers (partial experience still exists).
* Added special job "None", which serves as a catch all for players without a job.

## 2.3.3
* Added /jobs admininfo <playername>

## 2.3.2
* Changed WHEAT to CROPS-7 for Farmers
* Bundled h2.jar

## 2.3.1
* Added missing console commands for fishing

## 2.3.0
* Added fishing job

## 2.2.1
* Fixed issue where Redstone Ore wasn't giving money or experience (Normalized GLOWING_REDSTONE_ORE to REDSTONE_ORE)

## 2.2.0
* Removed Flatfile support in favor of H2

## 2.1.1
* Fixed duplicate titles on /reload

## 2.1.0
* Fixed whitespace issues with chat titles
* Added "restricted areas" preventing jobs from making money in admin defined areas
* Added locking to help prevent jobs disappearing from flatfile
* Removed iConomy 4 support
* Support for Bukkit build 935
* Small bug fixes and code cleanup