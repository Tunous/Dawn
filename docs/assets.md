# Building Assets

In `/assets` you'll find `tunous_dawn.afdesign`. This includes the app and notification icons for the app.
To build the assets, enter the export persona inside of Affinity Designer and export the slices to `/assets/export`.

## App Icons

In android studio, right click on `/app/src/main/res` folder and navigate to `New -> Image Asset`.
Complete the form with a resize of 70%. On the next page select `Res Directory: main` for Dawn assets,
and `Res Directory: debug` for Dusk assets.

## Splash Icons

Android studio plugin [Android Drawable Importer](https://github.com/MPArnold/android-drawable-importer-intellij-plugin)
is used to build the splash icons. Right click on `/app/src/main/res` and navigate to `New -> Batch Drawable Import`.
Complete the form for both Dawn and Dusk, selecting the appropriate res directory for each.

## Notification Icons

In android studio, right click on `/app/src/main/res` folder and navigate to `New -> Image Asset`.
Select `Icon Type: Notification Icons`. Complete the form with a padding of 0%. On the next page select
`Res Directory: main` for Dawn assets, and `Res Directory: debug` for Dusk assets.
