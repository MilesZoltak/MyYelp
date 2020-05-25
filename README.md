# MyYelp

## *Miles Zoltak*

**MyYelp** displays a list of search results from the Yelp API and displays the results in a scrollable list. 

Time spent: **10** hours spent in total

## Functionality 

The following **required** functionality is completed:

* [x] Ability to query the Yelp API to get results from a search query
* [x] The search results are displayed in a RecyclerView

The following **extensions** are implemented:

* [x] Added a Speed Dial FAB that houses buttons to customize "Search Term" and "Search Location"
* [x] Added optionality to use last known location in lieu of typing in location

## Video Walkthrough

Here's a walkthrough of implemented user stories:

<img src='https://imgur.com/RJFk62l' title='Video Walkthrough' width='' alt='Video Walkthrough' />

GIF created with [LiceCap](http://www.cockos.com/licecap/).

## Notes

I had a bit of trouble using the Speed Dial which I pulled from a 3rd part Git Repo, but I worked out the kinks and got things going.
I also struggled with grabbing the user's data, and for some reason on the very first call to getLastLocation() it returns null.  Once you
  click "My Location" a second time it grabs the location no problem, but I didn't spend the time to iron out that little kink before the
  deadline.
It's also not ideal that it automatically opens up with "New York Avocado Toast" on startup every time, but that's how we originally built
  it and I figured it wouldn't be that much of a problem to leave it in as long as the user can navigate where they want from there.  In
  my opinion it's the difference between displaying no information at the start versus irrelevant information.  I suppose the underlying
  philosophy is "done is better than perfect."

## License

    Copyright [2020] [Miles Zoltak]

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
