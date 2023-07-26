# ChangeLog #

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] ##

## [0.3.4] - 2023-07-26 ##

### Fixed ###

- Unable to update subscription amount value ([#18](https://github.com/AdGoji/mollie/issues/18)).

## [0.3.3] - 2023-07-25 ##

### Fixed ###

- Value `card-expiry-date` doesn't conform to spec for mandates ([#16](https://github.com/AdGoji/mollie/issues/16)).

## [0.3.2] - 2023-07-14 ##

### Fixed ###

- Response code 400 from Mollie API is not handled ([#13](https://github.com/AdGoji/mollie/issues/13)).

## [0.3.1] - 2023-07-13 ##

### Fixed ###

- Unable to produce JSON with `java.time.LocalDate` ([#11](https://github.com/AdGoji/mollie/issues/11)).

## [0.3.0] - 2023-07-12 ##

### Added ###

- New client option: `throw-exceptions?` ([#9](https://github.com/AdGoji/mollie/issues/9)).

### Changed ###

- Client option `:check-response` is renamed to `:check-response?` ([#9](https://github.com/AdGoji/mollie/issues/9)).

## [0.2.0] - 2022-06-07 ##

### Fixed ###

- Spec error during fetching all entities with pagination ([#7](https://github.com/AdGoji/mollie/issues/7)).

### Changed ###

- Response format for mandates and payments. Nested `details` map was
  brought to the top level with different namespace.

## [0.1.4] - 2023-06-05 ##

### Added ###

- README and documentation strings ([#3](https://github.com/AdGoji/mollie/issues/3)).

### Fixed ###

- Include spec to the classpath ([#5](https://github.com/AdGoji/mollie/issues/5)).

## [0.1.3] - 2023-06-29 ##

### Added ###

- Customers management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Payments management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Mandates management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Subscriptions management ([#1](https://github.com/AdGoji/mollie/issues/1)).

[unreleased]: https://github.com/AdGoji/mollie/compare/0.3.4..HEAD
[0.3.4]: https://github.com/AdGoji/mollie/compare/0.3.3..0.3.4
[0.3.3]: https://github.com/AdGoji/mollie/compare/0.3.2..0.3.3
[0.3.2]: https://github.com/AdGoji/mollie/compare/0.3.1..0.3.2
[0.3.1]: https://github.com/AdGoji/mollie/compare/0.3.0..0.3.1
[0.3.0]: https://github.com/AdGoji/mollie/compare/0.2.0..0.3.0
[0.2.0]: https://github.com/AdGoji/mollie/compare/0.1.4..0.2.0
[0.1.4]: https://github.com/AdGoji/mollie/compare/0.1.3..0.1.4
[0.1.3]: https://github.com/AdGoji/mollie/releases/tag/0.1.3
