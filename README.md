Lyric Here [![GitHub release](https://img.shields.io/badge/sample%20apk-2.0.0alpha.2-brightgreen.svg?style=flat)](https://github.com/markzhai/LyricHere/releases/download/v2.0-alpha.2/app-debug.apk)
==========
A music player focused on user experience of lyric.

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

- Show artwork in listview.
- Better implementation for Music player.
- Download lyric from server and upload to server.
- Add support lyric file encoding auto-recognize.
- Use Google Design Support UI.

Test
----
- Nexus 6 (5.1.1)
- OnePlus (4.3)

Screenshots
-----------

![Browse local music file](art/Screenshot_2015-03-20-17-07-26.jpg "Browse local music file")
![Fullscreen music player](art/Screenshot_2015-03-20-17-07-30.jpg "Fullscreen music player")
![Lyric explorer](art/Screenshot_2015-03-20-17-09-38.jpg "Lyric explorer")
![Lyric player](art/Screenshot_2015-03-20-17-11-09.jpg "Lyric player")
![Lyric encoding picker](art/Screenshot_2015-03-20-17-11-28.jpg "Lyric encoding picker")
![Notification](art/Screenshot_2015-09-09-23-12-51.jpg "Notification")

CHANGELOG
-------

- Add music player, refactor UI to material design.
- Add recent played lyric feature.
- A lyric player which can play local lrc files.

LIBRARY
-------

- Butter Knife
- DBFlow
- base-adapter-helper