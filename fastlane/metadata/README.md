# Play Store listings

Copy for [Google Play Console](https://play.google.com/console) listings, in [Fastlane supply](https://docs.fastlane.tools/actions/upload_to_play_store/) layout. Fastlane itself is not installed yet; paste these into Console until it is.

| File | Play Console field | Limit |
| --- | --- | --- |
| `title.txt` | App name | 30 characters |
| `short_description.txt` | Short description | 80 characters |
| `full_description.txt` | Full description | 4000 characters |
| `changelogs/<versionCode>.txt` | Release notes | 500 characters |

Locales:

- `en-GB` — canonical English (this is the source to translate from)
- `en-US` — same listing, US spelling
- `es-ES` — Spanish (Spain)
- `pt-BR` — Portuguese (Brazil)
- `it-IT` — Italian

