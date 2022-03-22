# One-Time Download (1TDL)

A file-sharing service that allows only 1 download per file. All uploaded files are password-encrypted and require the
correct password to download.

## Run/Test Environment Configuration

* Run working directory: `./run` (included in `.gitignore`)
* Run properties file: `./run/application.properties`
* Test working directory: `./test` (included in `.gitignore`)
* Test properties file: `./test/application.properties`

### Data Source Configuration

**MySQL**

* `spring.datasource.username=<username>`
* `spring.datasource.password=<password>`
* `spring.datasource.url=<url>`
* `spring.jpa.hibernate.ddl-auto=update` (recommended for testing)

### Storage Configuration

**AWS S3**

* `otdl.storage.module=s3`
* `otdl.storage.s3.region.static=<region code>`
* `otdl.storage.s3.credentials.access-key=<access key>`
* `otdl.storage.s3.credentials.secret-key=<secret key>`
* `otdl.storage.s3.bucket-name=<bucket name>`

**Local File System**

* `otdl.storage.module=local`
* `otdl.storage.local.location=<path>`

### Email Configuration

* `otdl.email.enable=<true|false>` (default: `false`): If true, remaining settings must be defined
* `otdl.email.host=<host name>`
* `otdl.email.port=<integer>`: 25 or 587 is recommended for SMTP
* `otdl.email.username=<username>`
* `otdl.email.password=<password>`
* `otdl.email.protocol=<protocol>` (default: `smtp`)
* `otdl.user.email-from-address=<user>@<mail server host name>`
* `otdl.user.email-confirm-host=<service host name>`