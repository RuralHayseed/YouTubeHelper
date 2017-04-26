# YouTubeHelper
An introduction to YouTube Data API (v3)
On YouTubeHelper

This is a small exercise in developing an understanding of the YouTube Data API (v3).  I have a need to access content definitions of a given YouTube channel, and to maintain those definitions externally as the channel is updated.  To that end, this simple application provides me that functionality, and in the process, I learn a little about the YouTube API.

The application implements two API features.  The PlaylistItems.List request is used to obtain a complete definition list of all videos in the upload playlist.  The Search.List request is used to obtain video definitions added to the upload playlist since a given date.  My expectation is that a PlaylistItems.List request provides a complete list of channel content, and that a periodic search will retrieve additions to the playlist since the last query.

The main API logic is in GetVideoList.  The getUploads method implements the two step sequence of retrieving channel content, which consists of retrieving the uploads playlist ID, and then using PlaylistItems.List to retrieve the full list of uploads.  The searchUploadListByDate method retrieves the incremental additions to the upload playlist.  Overloaded variants of these method provide additional functionality unrelated to the actual API requests.

I use an API key to authenticate the YouTube requests.  The location of the API key is defined in an external text file, which also contains other runtime parameters, and is located in the scripts directory.

<b>
License
</b><br>

Copyright (C) 2017 HayseedSoftware

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
