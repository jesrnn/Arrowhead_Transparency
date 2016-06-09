# Arrowhead_Transparency

To run, simply run the eclipse project. No arguments or configuration required (at this stage)


Using HttpRequester on Firefox:
Usage example:

POST http://[fdfd:55::80ff]:8000/translator
Content-Type: application/xml
<translatorSetup>
<providerName>coap</providerName>
<providerType>coap</providerType>
<providerAddress>coap://[fdfd::01]:5683</providerAddress>
<consumerName>http</consumerName>
<consumerType>http</consumerType>
<consumerAddress>http</consumerAddress>
</translatorSetup>
 -- response --
200 OK
Content-Type:  text/html
Content-Length:  93
Server:  Jetty(9.1.0.M0)

<translationendpoint><id>14076</id><ip>127.0.0.1</ip><port>64736</port></translationendpoint>



