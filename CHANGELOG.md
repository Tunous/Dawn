# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Changed

-   Limited images cache to 200 MB ([#381](https://github.com/Tunous/Dawn/pull/381))

## [0.12.2] - 2021-01-31

### Changed

-   Increased target API version to 30 ([#390](https://github.com/Tunous/Dawn/pull/390))

## [0.12.1] - 2021-01-29

### Fixed

-   Fix Gfycat ([#363](https://github.com/Tunous/Dawn/pull/363))
-   Fix post title selection after vote ([#365](https://github.com/Tunous/Dawn/pull/365))
-   Keep text in comment reply on screen rotation ([#366](https://github.com/Tunous/Dawn/pull/366))
-   Fix link previews ([#384](https://github.com/Tunous/Dawn/pull/384))

## [0.12.0] - 2021-01-10

### Added

-   Support for multiple accounts ([#329](https://github.com/Tunous/Dawn/pull/329))

## [0.11.0] - 2020-09-22

### Added

-   Icons for OP comments and up/downvote ([#305](https://github.com/Tunous/Dawn/pull/305))
-   Support for reddit galleries ([#312](https://github.com/Tunous/Dawn/pull/312))

### Changed

-   Increased minimum required API version to 23 ([#318](https://github.com/Tunous/Dawn/pull/318))

### Fixed

-   Correctly parse links with leading/trailing spaces ([#319](https://github.com/Tunous/Dawn/pull/319))
-   App icon on API versions lower than 26 ([#318](https://github.com/Tunous/Dawn/pull/318))

## [0.10.0] - 2020-08-14

### Added

-   Full height images style ([#207](https://github.com/Tunous/Dawn/pull/207))

### Changed

-   Add support for alternate Redgifs domain ([#281](https://github.com/Tunous/Dawn/pull/281))

### Fixed

-   Show smaller Imgur images when data saving mode enabled ([#256](https://github.com/Tunous/Dawn/pull/256))
-   Don't show too small reddit previews ([#264](https://github.com/Tunous/Dawn/pull/264))
-   Fix sharing an image multiple times ([#280](https://github.com/Tunous/Dawn/pull/280))
-   Allow grouping of media download notifications ([#285](https://github.com/Tunous/Dawn/pull/285))
-   Workaround invisible download notifications on Android 11 ([#282](https://github.com/Tunous/Dawn/issues/282))
-   Fix downloaded media not appearing in gallery ([#288](https://github.com/Tunous/Dawn/pull/288))
-   Notification icon ([#269](https://github.com/Tunous/Dawn/issues/269))

## [0.9.2] - 2020-06-13

### Added

-   Support for Redgifs ([#219](https://github.com/Tunous/Dawn/pull/219))

### Changed

-   New Icon ([#212](https://github.com/Tunous/Dawn/pull/212))

### Fixed

-   Check if user is logged in before allowing replies to a submission ([#215](https://github.com/Tunous/Dawn/pull/215))
-   Fixes for Gfycat and Giphy media ([#211](https://github.com/Tunous/Dawn/issues/211))
-   Fix playing of reddit hosted videos ([#210](https://github.com/Tunous/Dawn/pull/210))
-   Imgur images (up)loading ([#202](https://github.com/Tunous/Dawn/pull/202))
-   Always show fullsize imgur images ([#209](https://github.com/Tunous/Dawn/pull/209))
-   Fix loading of Imgur images ([#202](https://github.com/Tunous/Dawn/pull/202))

## [0.9.1] - 2020-05-05

### Fixed

-   Handle exceptions while opening a browser ([#193](https://github.com/Tunous/Dawn/pull/193))
-   Crash when selecting submission title ([#194](https://github.com/Tunous/Dawn/pull/194))
-   Problem with API permissions ([#196](https://github.com/Tunous/Dawn/pull/196))

## [0.9.0] - 2020-05-03

### Added

-   Large image previews ([#87](https://github.com/Tunous/Dawn/pull/87))
-   Inline text selection ([#177](https://github.com/Tunous/Dawn/pull/177))
-   Submission title selection ([#182](https://github.com/Tunous/Dawn/pull/182))
-   Selftext selection ([#178](https://github.com/Tunous/Dawn/pull/178))

### Changed

-   Renamed application to "Dawn" ([#74](https://github.com/Tunous/Dawn/pull/74))
-   Wrap comment byline when it is too long to fit in one line  ([#145](https://github.com/Tunous/Dawn/pull/145))

### Fixed

-   Occasional crash when entering login screen ([#103](https://github.com/Tunous/Dawn/pull/103))
-   Crash when navigating to user profile ([#109](https://github.com/Tunous/Dawn/pull/109))
-   Image loading issues due to http urls ([#183](https://github.com/Tunous/Dawn/pull/183))

## [0.8.0] - 2019-08-22

### Added

-   Subscribing to /r/all ([#28](https://github.com/Tunous/Dawn/pull/28))
-   View parent submission of crosspost ([#29](https://github.com/Tunous/Dawn/pull/29))
-   Links to repository and changelog in about screen ([#42](https://github.com/Tunous/Dawn/pull/42))
-   Colored comments depth level ([#49](https://github.com/Tunous/Dawn/pull/49))

### Fixed

-   Clicking on notification doesn't open downloaded image ([#35](https://github.com/Tunous/Dawn/pull/35))
-   Notifications for downloaded videos show incorrect thumbnails ([#41](https://github.com/Tunous/Dawn/pull/41))
-   Video cache is not working ([#50](https://github.com/Tunous/Dawn/pull/50))
-   Trying to download the same picture twice crashes app ([#57](https://github.com/Tunous/Dawn/pull/57))

## [0.7.2] - 2019-08-09

### Added

-   Configured automatic releases to GitHub.

## [0.7.1] - 2019-08-09

### Fixed

-   YouTube links not opening when YouTube application is not installed ([upstream#17](https://github.com/saket/Dank/pull/17))
-   Login not working when user has enabled 2FA ([#32](https://github.com/Tunous/Dawn/pull/32))
-   Login screen not closing when user declines reddit access ([#34](https://github.com/Tunous/Dawn/pull/34))

## [0.7.0] - 2019-07-09

### Added

-   Highlight sticky submissions ([#16](https://github.com/Tunous/Dawn/pull/16))
-   Submission gesture customization ([#3](https://github.com/Tunous/Dawn/pull/3))

### Changed

-   Do not display submission subreddit if it's same as viewed subreddit ([9ad9e121](https://github.com/Tunous/Dawn/commit/9ad9e121a2e7633e01c49c1ebf6e1b9dd114a2f0))
-   Better detection for user and subreddit links ([#17](https://github.com/Tunous/Dawn/pull/17))

## [0.6.3] - 2019-01-20

### Added

-   Toggle to show/hide comments count on list ([#2](https://github.com/Tunous/Dawn/pull/2))
-   Thumbnails on the left side of submission ([#4](https://github.com/Tunous/Dawn/pull/4))
-   Long click options menu for in-text links ([#11](https://github.com/Tunous/Dawn/pull/11))
-   Sorting submissions by "Best" ([efcf181f](https://github.com/Tunous/Dawn/commit/efcf181f3bd7952aa9c45c035b39bd91c26d748a))

### Changed

-   Display when "Poll for new messages" is disabled ([8512307c](https://github.com/Tunous/Dawn/commit/8512307cc1bb47129e674c14e6a4219beaba032a))

### Fixed

-   Crash after posting comment reply ([#21](https://github.com/Tunous/Dawn/pull/21))
-   Crash when duplicate submissions are downloaded ([71404c12](https://github.com/Tunous/Dawn/commit/71404c12fe8cee3c8770287ebef5eff52c10d724))
-   Crash when clicking on thumbnail for self post ([7d54df6d](https://github.com/Tunous/Dawn/commit/7d54df6dc2ed96a16a549b3ac130e53105008244))
-   Pull to collapse working incorrectly in split screen ([3f1500e0](https://github.com/Tunous/Dawn/commit/3f1500e0b2218f31c5a8e592082a7b8d646ca271))
-   Position of option buttons in image viewer ([6d7c738c](https://github.com/Tunous/Dawn/commit/6d7c738cfefbad7776f2a311770182bef6b6c6f3))
-   Position of video player controls in video viewer ([97780547](https://github.com/Tunous/Dawn/commit/97780547b69ab4a4c72a8be8303323f073b745b1))
-   Incorrect height of videos in video viewer ([4186b3ed](https://github.com/Tunous/Dawn/commit/4186b3ed2467318a3b27113a65bc791ec6c9ff8a))
-   Crash when swiping item further than their width ([c9051f75](https://github.com/Tunous/Dawn/commit/c9051f7586419a4bef0e856c18cb70831d43839b))
-   Crash when inserting heading or quote at last empty line ([948136ef](https://github.com/Tunous/Dawn/commit/948136eff59987fb68c334c1829d48d8c60123eb))
-   Walkthrough screen refreshing on every action ([b20345fe](https://github.com/Tunous/Dawn/commit/b20345fedfe4ee3ec4d9dc799c09c7d385db5a37))
-   Prevent subreddit search field from going fullscreen ([5856531e](https://github.com/Tunous/Dawn/commit/5856531e15f19e366f9556802dd90c00087d2d8e))

## [0.6.2] and earlier

Changes before the fork has been made.

[Unreleased]: https://github.com/Tunous/Dawn/compare/0.12.2...HEAD

[0.12.2]: https://github.com/Tunous/Dawn/compare/0.12.1...0.12.2

[0.12.1]: https://github.com/Tunous/Dawn/compare/0.12.0...0.12.1

[0.12.0]: https://github.com/Tunous/Dawn/compare/0.11.0...0.12.0

[0.11.0]: https://github.com/Tunous/Dawn/compare/0.10.0...0.11.0

[0.10.0]: https://github.com/Tunous/Dawn/compare/0.9.2...0.10.0

[0.9.2]: https://github.com/Tunous/Dawn/compare/0.9.1...0.9.2

[0.9.1]: https://github.com/Tunous/Dawn/compare/0.9.0...0.9.1

[0.9.0]: https://github.com/Tunous/Dawn/compare/0.8.0...0.9.0

[0.8.0]: https://github.com/Tunous/Dawn/compare/0.7.2...0.8.0

[0.7.2]: https://github.com/Tunous/Dawn/compare/0.7.1...0.7.2

[0.7.1]: https://github.com/Tunous/Dawn/compare/0.7.0...0.7.1

[0.7.0]: https://github.com/Tunous/Dawn/compare/0.6.3...0.7.0

[0.6.3]: https://github.com/Tunous/Dawn/compare/0.6.2...0.6.3

[0.6.2]: https://github.com/Tunous/Dawn/releases/tag/0.6.2
