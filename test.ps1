
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwb3N0bWFuX3VzZXIiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc4NjYzNTI4OCwiZXhwIjoxNzg2NzIxNjg4fQ.JMPmqFR3PdajQAujW5XEz8-reK3_Lxw1BdTVCopVdPE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "--- TEST /api/auth/me ---"
Invoke-RestMethod -Uri "http://localhost:8000/api/auth/me" -Method GET -Headers $headers | ConvertTo-Json

Write-Host "--- TEST createRoom ---"
$body = @{
    query = "mutation { createRoom(name: `"Dev Room`", description: `"Testing room`") { id name description } }"
} | ConvertTo-Json
$roomResponse = Invoke-RestMethod -Uri "http://localhost:8000/graphql" -Method POST -Headers $headers -Body $body
$roomResponse | ConvertTo-Json -Depth 10
$roomId = $roomResponse.data.createRoom.id

Write-Host "--- TEST rooms ---"
$bodyRooms = @{
    query = "query { rooms { id name } }"
} | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8000/graphql" -Method POST -Headers $headers -Body $bodyRooms | ConvertTo-Json -Depth 10

if ($roomId) {
    Write-Host "--- TEST sendMessage ---"
    $bodyMsg = @{
        query = "mutation { sendMessage(roomId: `"$roomId`", senderUsername: `"postman_user`", content: `"Hello World`") { id content sentAt } }"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "http://localhost:8000/graphql" -Method POST -Headers $headers -Body $bodyMsg | ConvertTo-Json -Depth 10
}

