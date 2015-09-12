Lyric Here [![GitHub release](https://img.shields.io/badge/sample%20apk-2.0.0beta-brightgreen.svg?style=flat)](https://github.com/markzhai/LyricHere/releases/download/v2.0-beta/lyric-here.apk)
==========
Material design的音乐/歌词播放器，带独立的歌词播放模块。使用新的android.media.MediaMetadata系列API实现，支持API 14以上的系统。

功能
----

- 本地音乐浏览和播放。
- 状态栏：音乐播放控件、歌词滚动控件。
- 独立的歌词播放功能，支持上下滚动和高亮色，各种编码设定。
- 接受流行播放器的播放广播，并在状态栏中提示歌词通知。

TODO(欢迎Pull)
--------------

- 使用Design Support库（如抽屉和歌词模块的tab）.
- 更好地使用音乐播放界面，全面界面支持直接显示歌词。
- 在服务器下载歌词，并支持用户上传。
- 支持歌词编码自动识别。

已测试
------
- Nexus 6 (5.1.1)
- OnePlus (4.3)

截图
-----------

![Browse local music file](art/Screenshot_2015-09-12-23-14-37.jpg "Browse local music file")
![Fullscreen music player](art/Screenshot_2015-09-12-21-13-22.jpg "Fullscreen music player")
![Lyric explorer](art/Screenshot_2015-09-12-21-13-40.jpg "Lyric explorer")
![Lyric player](art/Screenshot_2015-03-20-17-11-09.jpg "Lyric player")
![Lyric encoding picker](art/Screenshot_2015-03-20-17-11-28.jpg "Lyric encoding picker")
![Notification](art/Screenshot_2015-09-09-23-12-51.jpg "Notification")
![Lock Screen Background](art/Screenshot_2015-09-12-22-43-59.jpg "Lock Screen Background")

第三方库
--------

- Android Support Library
- Butter Knife
- DBFlow
- IcePick
- Mosby