# Security 

## Overview

This service issues JWT access tokens using client credentials
(`client_id` and `client_secret`). It is intended for trusted internal clients.

## Authentication

- Endpoint: `POST /token`
- Request: `client_id`, `client_secret`
- Response: JWT Bearer token

## Token

- Type: Bearer (JWT)
- Expiry: 30 minutes
- Contains client ID and roles

## Client Secrets

- Client credentials are configured via application properties
- Secrets must not be committed to source control
- Use environment variables or a secret manager

## Transport Security

- HTTPS is required
- Do not log or expose tokens or secrets

## Reporting Issues

Report security issues privately to the maintainers.
