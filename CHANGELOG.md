# Jobs plugin change log summary

## 2.12.0
* Support for Mojang UUID (converts to new database table format)
* Added async support for economy plugins

## 2.11.4
* Bugfix CraftItemEvent for invalid crafting events
* Updated translations (fr)
* Added new translations (et, cs)

## 2.11.3
* Added translations (it, ko)
* Updated translations (de)
* When using add-xp-player, added workaround to give player fraction of xp points (use dicerolls) when dealing with non-whole numbers
* Refactored inventory listeners for 1.5.2-R1.0

## 2.11.2
* Added translations (pt, no)
* Fixed output of special characters in multiple translations
* Permissions are only applied in worlds where players have world permission (jobs.world.WORLD_NAME)
* Minor change to how permissions are applied have it behave more like 2.10.2 (however, positive still wins over negative)

## 2.11.1
* Fixed experience per level and titles which were incorrectly being calculated under certain circumstances
* Added translations (pl, zh)

## 2.11.0
* Finished modularizing code (ready for Spout and Bukkit API ports!)
* Added /jobs help command
* Added description field in jobConfig.yml that shows up in /jobs browse command
* Fixed ordering of permissions, "true" will always win over "false" now.
* Made Vault completely optional, will use "blackhole" economy if Vault is missing
* Added multiple translations (es, fr, jp, ru)

## 2.10.2
* Fixed /jobs reload command
* Faster permission handling
* Added configuration toggle to allow for adding Jobs xp to player's minecraft XP bar
* Slightly modified /jobs info to make it easier to read
* Added better error handling for some configuration mishaps

## 2.10.1
* Fixed error when using player-only commands in console
* Fixed player notices in some admin commands

## 2.10.0
* Added full i18n support, removed messageConfig.yml (you may delete this file from your plugins folder)
* Modified payment handling to retry withdrawals if there is insufficient funding
* Implemented Anvil Reparing (Repair keyword)

## 2.9.2
* Added color coding to income in /jobs browse
* Changed behavior of REDSTONE_ORE, removed hacks, added configuration warning
* Fixed most admin commands to work better with offline players
* Reworked entire save system
* Potentially fixed issues with Tekkit and other client mods behaving badly (untested)

## 2.9.1
* Fixed crafting and smelting with subtypes
* Fixed some errors on shutdown
* Fixed issues causing permissions not to be granted to admins
* Fixed issues on some servers causing a permission error on player join

## 2.9.0
* Fixed some issues with World permissions
* Rewrote command handling
* Fixed issues with negative economy amounts
* Fixes some issues with economy payments causing lag spikes
* Implemented Bukkit Async chat events 
* Potentially fixed issues with IndustrialCraft (and other client mods, untested)

## 2.8.5
* Improved reading of mob names in config file
* Periodic saving is done in a separate thread
* Player logins and logouts are handled in a separate thread

## 2.8.4
* Fixed H2 driver
* Implemented Brewing
* Implemented Enchanting
* Added batch size flags for economy payments to help reduce tick lag
* Tasks now restart on reload
* Rewrote job task code to be more modular

## 2.8.3
* Fixed leaving jobs without having to logout

## 2.8.2
* Allow payment batching to be configurable
* Fixed H2 dependency conflicts

## 2.8.1
* Fixed NPE fix with smelting configuration

## 2.8.0
* Crafting should no longer grant XP when crafting fails
* Added support for 3rd party chat plugins
* Added smelting

## 2.7.4
* messageConfig.yml should no longer disapper if there are syntax errors
* Test equations during configuration loading stage to prevent errors with buggy formula

## 2.7.3
* jobConfig.yml should no longer disapper if there are syntax errors
* Permissions should now register correctly on world load

## 2.7.2
* Missing vault errors are handled more gracefully
* Fixed database locking issues with SQLite
* Jobs can grant permissions to users

## 2.7.1
* Removed empty sections on /jobs info
* Fixed broken MySQL
* You WILL need to update your "mysql-url" configuration

## 2.7.0
* Vault now required for Economy support
* Added crafting support without Spout
* Simplified permissions for joining jobs
* Simplified permissions for disabling in worlds
* Configuration errors are handled more gracefully
* Modified all configs to auto create if they don't exist
* Added SQLite database type
* Added toggle to disable in Creative mode
* Changed behavior of chat honorifics
* Removed Stats support

## 2.6.3
* Updated to Bukkit 1.1 Event System
* Updated off old Bukkit Configuration System

## 2.6.2
* Fixed linking issues with BOSEconomy
* Fixed NPE due to double onPlayerQuit events

## 2.6.1
* Fixed NPE with crafting
* Removed ugly timeout hack
* Fixed rounding issues when using BOSEconomy 7
* Buffered payments to reduce lag created by economy plugins

## 2.6.0
* Added workaround to MySQL timeout issues
* Removed Permissions 3 support, converted to DinnerPerms

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