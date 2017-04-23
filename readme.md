# What is this?

A bot sample for slack which reply paticular cell's value from google spread sheet written by Scala.

# Build

> sbt assembly

# Usage

> java -cp slackScalaSample-assembly-0.0.1.jar com.axtstar.slackScalaSample.Client \
   -t [slack token] \
   -c [google OAuth2 path to json] \
   -s [Google spreadsheet spread key] \
   -r "sheet!range"
