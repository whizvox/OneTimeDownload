# One-Time Download (1TDL)

A file-sharing service that allows only 1 download per file. All uploaded files are password-encrypted and require the
password to download.

## Run/Test Environment Configuration

* Run working directory: `./run` (included in `.gitignore`)
* Run properties file: `./run/application.properties`
* Test working directory: `./test` (included in `.gitignore`)
* Test properties file: `./test/application.properties`

### Data Source Configuration

**MySQL**

* `spring.datasource.username: <username>`
* `spring.datasource.password: <password>`
* `spring.datasource.url: <url>`

### Storage Configuration

**AWS S3**

* `otdl.storage.module: s3`
* `otdl.storage.s3.region.static: <region code>`
* `otdl.storage.s3.credentials.access-key: <access key>`
* `otdl.storage.s3.credentials.secret-key: <secret key>`
* `otdl.storage.s3.bucket-name: <bucket name>`

**Local File System**

* `otdl.storage.module: local`
* `otdl.storage.local.location: <path>`