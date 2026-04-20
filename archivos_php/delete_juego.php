<?php
require_once 'config.php';

$id = $_POST['id'] ?? '';
$user_id = $_POST['user_id'] ?? '';

if (empty($id) || empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Faltan campos']);
    exit;
}

$stmt = $conn->prepare("DELETE FROM juegos WHERE id=? AND user_id=?");
$stmt->bind_param("ii", $id, $user_id);

if ($stmt->execute()) {
    echo json_encode(['success' => true]);
} else {
    echo json_encode(['success' => false, 'message' => 'Error al eliminar']);
}

$stmt->close();
$conn->close();
?>
