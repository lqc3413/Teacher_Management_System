import urllib.request
import json

# 1. Generate hash using Spring BCryptPasswordEncoder
resp = urllib.request.urlopen('http://localhost:8080/api/dev/encode?password=admin123')
data = json.loads(resp.read().decode())
pw = data['data']['encodedPassword']

# 2. Verify it matches
encoded_pw = urllib.request.quote(pw)
resp2 = urllib.request.urlopen('http://localhost:8080/api/dev/verify?password=admin123&hash=' + encoded_pw)
data2 = json.loads(resp2.read().decode())

print("Generated hash:", pw)
print("Verification:", data2['data']['matches'])
print()
print("=== Please run this SQL in DBeaver ===")
print("USE teacher_mgmt;")
print("UPDATE users SET password = '" + pw + "' WHERE username = 'admin';")
