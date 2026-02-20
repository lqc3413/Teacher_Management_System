import urllib.request
import json

resp = urllib.request.urlopen('http://localhost:8080/api/dev/encode?password=admin123')
data = json.loads(resp.read().decode())
pw = data['data']['encodedPassword']

sql = "USE teacher_mgmt;\nUPDATE users SET password = '" + pw + "' WHERE username = 'admin';\n"

with open('update_pw.sql', 'w') as f:
    f.write(sql)

print("SQL written to update_pw.sql")
print("Content:")
print(sql)
