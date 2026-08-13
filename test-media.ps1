
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJwb3N0bWFuX3VzZXIiLCJyb2xlIjoiVVNFUiIsImlhdCI6MTc4NjYzNTI4OCwiZXhwIjoxNzg2NzIxNjg4fQ.JMPmqFR3PdajQAujW5XEz8-reK3_Lxw1BdTVCopVdPE"
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host "--- TEST create session ---"
$body = @{ roomId = "test-video-room" } | ConvertTo-Json
$sessionRes = Invoke-RestMethod -Uri "http://localhost:8000/api/media/sessions" -Method POST -Headers $headers -Body $body
$sessionRes | ConvertTo-Json

$sessionId = $sessionRes.sessionId
if ($sessionId) {
    Write-Host "--- TEST generate connection token ---"
    $connRes = Invoke-RestMethod -Uri "http://localhost:8000/api/media/sessions/$sessionId/connections" -Method POST -Headers $headers
    $connRes | ConvertTo-Json
}

