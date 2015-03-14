# Track-and-Trace #

# About #

This project is for the 'TrackAndTrace' Android application that allows you check the status of items sent via the New Zealand Post (NZPost) trace and trace service.

Note: this application utilises the NZPost [Tracking API](http://www.nzpost.co.nz/business/iphone-apps-apis/developer-centre/tracking-api) but is in no way affiliated with NZ Post. As a result please do not call NZPost regarding issue or fault with this application - raise an 'issue' on this site instead ;)

## Howto ##

### User Guide ###

  * Simply type in the tracking code(s) or,
    * menu -> scan the barcode(s) of the items you wish to track (requires ZXing)
  * Check the box beside the tracking code if you wish to save the item (for easy update check)
  * Hit the 'Run Search' button

#### Additional Features / Settings ####

  * menu -> settings for a few additional options such as:
    * The base post URL
    * Number of search boxes
    * Saved items (comma separated list)
  * menu -> postcode to find your current postcode from location services (v1.2+)

### Screenshots ###

| ![https://lh3.googleusercontent.com/_ZQm5YtCKEqM/Ta1tsExllOI/AAAAAAAAAKU/lGrgc2-fTDE/s400/TrackAndTrace.png](https://lh3.googleusercontent.com/_ZQm5YtCKEqM/Ta1tsExllOI/AAAAAAAAAKU/lGrgc2-fTDE/s400/TrackAndTrace.png) | ![https://lh5.googleusercontent.com/_ZQm5YtCKEqM/Ta0qUTMKqzI/AAAAAAAAAKA/JJ3XgIT3vL0/s400/TrackAndTrace2.png](https://lh5.googleusercontent.com/_ZQm5YtCKEqM/Ta0qUTMKqzI/AAAAAAAAAKA/JJ3XgIT3vL0/s400/TrackAndTrace2.png) |
|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

### Build Notes ###

#### API key ####
Check out the code and create a file res/values/apikey.xml with the contents below. Include your API key as the api\_key value.
```
<?xml version="1.0" encoding="utf-8"?>
<!-- The NZPost API key  -->
<resources>
	<string name="api_key"></string>
</resources>
```

#### Automated builds ####
To avoid having to manual change debugging setting from test to production builds you can try an automated build. Check the build.xml file for entries you will need in a local.properties and run ant on the build.xml file.

## Bugs ##

See the issues tab - please raise an issue if you have a bug or an enhancement request.