#!/bin/bash
openssl genpkey -algorithm RSA -out jwt/privateKey.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in jwt/privateKey.pem -out jwt/publicKey.pem