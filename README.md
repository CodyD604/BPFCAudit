# BPFCAudit

BPFCAudit is an empirical policy development engine for use with [BPFContain-rs](https://github.com/willfindlay/bpfcontain-rs).

BPFCAudit's UI may be found at [BPFCAudit-ui](https://github.com/CodyD604/BPFCAudit-ui).

## Dependencies

* [BPFContain-rs](https://github.com/willfindlay/bpfcontain-rs)
* Maven
    * `sudo apt install maven`
* Java 11+ (untested with 8+ but should work)
    * `sudo apt install default-jre`
* [PostgreSQL](http://postgresguide.com/setup/install.html)


## Running

1. Install the above dependencies
2. Clone: `git clone https://github.com/CodyD604/BPFCAudit/ && cd BPFCAudit`
3. Run: `mvn spring-boot:run`

## Usage

1. Try out the APIs with the provided [postman](postman) collection and environment
2. Ensure that you have confined a program if you wish to capture audit event data after initiating an audit