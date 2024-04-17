  * [OAuth 2.0](https://www.rfc-editor.org/rfc/rfc6749)
  * [OAuth 2.0 Bearer Token](https://www.rfc-editor.org/rfc/rfc6750)
  * [Json Web Token](https://datatracker.ietf.org/doc/html/rfc7519)
  * [Open Id Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
  * [Open Id Connect Dynamic Registration](https://openid.net/specs/openid-connect-registration-1_0.html)
  * OIDC authentication request (GET or POST) parameters:
    * scope: must contain "openid"
    * response_type: "code" for the authorization code flow, "id_token" for the implicit flow
    * client_id: Client Identifier valid at the Authorization Server
    * redirect_uri: Redirection URI to which the response will be sent. This URI MUST exactly match one of the Redirection URI values for the Client pre-registered at the OpenID Provider.
    * state: optional; Opaque value used to maintain state between the request and the callback. Typically, Cross-Site Request Forgery (CSRF, XSRF) mitigation is done by cryptographically binding the value of this parameter with a browser cookie.
  * The following is the non-normative example request that would be sent by the User Agent to the Authorization Server in response to the HTTP 302 redirect response by the Client: ```shell
GET /authorize?
    response_type=code
    &scope=openid%20profile%20email
    &client_id=s6BhdRkqt3
    &state=af0ifjsldkj
    &redirect_uri=https%3A%2F%2Fclient.example.org%2Fcb HTTP/1.1
  Host: server.example.com```