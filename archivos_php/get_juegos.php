<?php
require_once 'config.php';

$user_id = $_GET['user_id'] ?? '';

if (empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Falta user_id']);
    exit;
}

$stmt = $conn->prepare("SELECT * FROM juegos WHERE user_id = ? ORDER BY fecha_ultima_sesion DESC");
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$juegos = [];
while ($row = $result->fetch_assoc()) {
    $juegos[] = $row;
}

echo json_encode(['success' => true, 'juegos' => $juegos]);
$stmt->close();
$conn->close();
?>
