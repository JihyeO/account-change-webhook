import hmac
import hashlib

secret = b'test'
msg = b'{"type":"ACCOUNT_UPDATED","accountKey":"abc123","provider":"bank","data":{"email":"user@example.com"}}'
print(hmac.new(secret, msg, hashlib.sha256).hexdigest())
