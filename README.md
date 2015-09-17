Lyric Here [![GitHub release](https://img.shields.io/badge/sample%20apk-2.0.0beta-brightgreen.svg?style=flat)](https://github.com/markzhai/LyricHere/releases/download/v2.0-beta/lyric-here.apk)
==========
Material design music and lyric player. Using Android's new android.media.MediaMetadata series api to implement. ([For Chinese 中文戳这里](https://github.com/markzhai/LyricHere/blob/master/README_CN.md))

Pre-requisites
--------------
- Android SDK v14

Features
-----------
- Local music browser and player.
- Music player widget, notification widget.
- Lyric directly refresh on notification, see it whenever you want.
- Powerful LyricView which supports scrolling up and down to change offset.
- Receive broadcast from popular music players and pop up lyric open notification.

TODO(Pull request is welcomed)
------------------------------

- Find some nice icons to replace currect genre, artist, album icon.
- Use Google Design Support UI (AppBar, CoordinatorLayout, etc.)
- Better implementation for Music player, show lyric directly.
- Download lyric from server and upload to server.
- Add support lyric file encoding auto-recognize.

Tested
------
- Nexus 6 (5.1.1)
- OnePlus (4.3)

Screenshots
-----------
![Browse local music file](art/Screenshot_2015-09-12-23-14-37.jpg "Browse local music file")
![Fullscreen music player](art/Screenshot_2015-09-12-21-13-22.jpg "Fullscreen music player")
![Lyric explorer](art/Screenshot_2015-09-12-21-13-40.jpg "Lyric explorer")
![Lyric player](art/Screenshot_2015-03-20-17-11-09.jpg "Lyric player")
![Lyric encoding picker](art/Screenshot_2015-03-20-17-11-28.jpg "Lyric encoding picker")
![Notification](art/Screenshot_2015-09-09-23-12-51.jpg "Notification")
![Lock Screen Background](art/Screenshot_2015-09-12-22-43-59.jpg "Lock Screen Background")

LIBRARY
-------
- Android Support Library (cardview, appcompat, design, mediarouter)
- [Butter Knife](https://github.com/JakeWharton/butterknife)
- [DBFlow](https://github.com/Raizlabs/DBFlow)
- [Icepick](https://github.com/frankiesardo/icepick)
- [mosby](https://github.com/sockeqwe/mosby)
- [android-ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference)
- [LyricView](https://github.com/markzhai/LyricView)