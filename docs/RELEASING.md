# On-demand releases

Update `versionName` and `versionCode` in `app/build.gradle.kts`, commit/push to
`main`, then choose **Actions > Release > Run workflow > main**. Enter short
English Google Play "What's new" notes in the text field that appears. There
are no version inputs or increments: Gradle's configured name is
the Git tag/release name, and the configured code is sent to Play unchanged.
After manually releasing 1.17, update both values for the next release before
running this workflow.

## Workflow

The workflow has two jobs:

1. **submit_play**: build, JVM tests, lint, sign the AAB using your upload key,
   attest its build provenance, and use `r0adkll/upload-google-play` to submit to
   `production` with `status: completed` and `changesNotSentForReview: false`.
2. **github_release**: download and verify Google's universal APK, attest its
   distribution, and use `softprops/action-gh-release` to create the matching tag
   at the checked-out commit and publish the APK and verification assets.

No auto-increment, staged rollout, internal track, or manually approved GitHub
release is required. Google review and account requirements still apply. Disable
**Managed publishing** in Play Console if you want approved changes to go live
without a separate publish click. GitHub can publish before the Play update is live.

The required text field supplies Google Play's `en-US` "What's new" notes. It
must be nonempty and at most 500 Unicode characters. The workflow checks it
before building and submits it with the production release. GitHub's separately
generated release notes are unaffected.

Existing tags stop a fresh submission. Play rejects reused version codes. Runs are
serialized. Actions are pinned to reviewed commit hashes.

The small `scripts/download-play-apk.sh` helper fills the gap between the two
release actions: polling Google's generated-APK API, selecting the configured
signer, and checking the downloaded Android signature/package/version. It does
not create releases or submit Play edits. It stops if no matching universal APK
is available; it never substitutes an APK signed with the upload key.

## One-time setup

Create a GitHub environment named **release**, restrict deployment branches to
`main`, and leave required reviewers off for an uninterrupted manual run.
Configure the following environment variables and secrets.

| Variable | Value |
|---|---|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | Full `projects/.../locations/global/workloadIdentityPools/.../providers/...` identifier |
| `PLAY_SERVICE_ACCOUNT` | Google service account email |
| `PLAY_APP_SIGNING_SHA256` | App-signing certificate SHA-256 from Play Console |
| `UPLOAD_CERTIFICATE_SHA256` | Upload certificate SHA-256 from Play Console |

| Secret | Value |
|---|---|
| `UPLOAD_KEYSTORE_BASE64` | Base64 of your existing upload keystore, without line breaks |
| `UPLOAD_KEYSTORE_PASSWORD` | Keystore password |
| `UPLOAD_KEY_ALIAS` | Signing alias |
| `UPLOAD_KEY_PASSWORD` | Private-key password (often the same as the store password) |

The fingerprints supplied during setup were:

- App signing: `CA:75:21:C1:50:1D:5F:C2:87:B6:FB:A4:DF:17:45:11:81:18:56:5D:1E:36:A9:C2:23:2B:35:39:20:FC:4F:1D`
- Upload: `38:DB:E9:AC:C4:FE:E7:9C:F3:80:AA:49:CB:3D:F7:47:3E:E5:90:4A:4F:05:C5:9B:E1:BE:F0:F1:CA:CD:DF:B2`

Confirm them in Play Console. These are public fingerprints, not private keys.
For a signing-key upgrade, review cross-version update compatibility before
changing the allowed signer. Google may generate different signer groups; the
helper only accepts a universal APK signed with the configured certificate.

### Google authentication

Use GitHub OIDC with Google Workload Identity Federation, not a long-lived service
account JSON private key. Enable the Google Play Android Developer API. Give the
service account access to **TapLock only** in Play Console, including app information
and production release permissions.

Configure the workload identity provider to trust only this repository (prefer
immutable repository/owner IDs), `refs/heads/main`, and this release workflow.
Grant the matching principal `roles/iam.workloadIdentityUser` on the service
account. Project-wide Owner/Editor roles are unnecessary.

`google-github-actions/auth` generates a temporary external-account credential
file. The Play upload action passes it to Google's authentication library through
`GOOGLE_APPLICATION_CREDENTIALS`. No Google private key is committed or stored in
GitHub secrets. The second job obtains a short-lived Play API access token.

Allow this workflow's GitHub token to create tags and releases under your repository
rules. Restrict who can modify `main` and the workflow; Gradle and Actions execute
trusted repository code with access to release credentials.

## Recovery

If **submit_play succeeded** but **github_release failed**, choose **Re-run failed
jobs** on that run. GitHub retains the successful job's version and AAB digest
outputs. Only download/publication is retried, so Play is not submitted again.
A retry rejects a tag pointing elsewhere and refuses to replace an existing APK
with different bytes. Verification assets may be regenerated for the same APK.

Do not start a fresh run or re-run all jobs merely to retry GitHub publication:
that attempts the Play upload again and the version code may already be used.
If the Play job itself fails or times out near submission, inspect Play Console
before retrying; submission may already have succeeded. There is no atomic
transaction between Play and GitHub, and no guessed recovery from an ambiguous
Play commit. A maintainer may need to finish that release manually.

## Signing and verification

Android APK signing and Sigstore serve different purposes. Google's signature
preserves Android update identity. The APK's custom Sigstore **Play distribution**
attestation records retrieval and verification, linking the APK digest, source
commit, original AAB digest, and workflow run. It does not claim GitHub built the
APK bytes. The AAB's separate build-provenance attestation is stored by GitHub.

Release assets: `TapLock-VERSION.apk`, `SHA256SUMS`, `play-predicate.json`, and
`apk.sigstore.json`. The AAB is not attached to the GitHub release.

```bash
gh attestation verify TapLock-VERSION.apk --repo modelorona/TapLock \
  --predicate-type https://github.com/modelorona/TapLock/attestations/play-distribution/v1
apksigner verify --print-certs TapLock-VERSION.apk
```

Local validation (does not publish):

```bash
actionlint .github/workflows/release.yml
shellcheck scripts/download-play-apk.sh
./gradlew :app:writeReleaseMetadata testDebugUnitTest lint bundleRelease
```

Live submission still requires configuring the credentials and deliberately running
the workflow. API integration and account permissions cannot be proven by local
builds or mocked tests.

References: [Play upload action](https://github.com/r0adkll/upload-google-play),
[GitHub release action](https://github.com/softprops/action-gh-release),
[Play generated APK API](https://developers.google.com/android-publisher/api-ref/rest/v3/generatedapks/list),
[GitHub attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations),
[Google authentication](https://github.com/google-github-actions/auth).
