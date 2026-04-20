<?php
require_once 'config.php';

$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Faltan campos']);
    exit;
}

$stmt = $conn->prepare("SELECT id, username, password, foto_perfil FROM usuarios WHERE email = ?");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(['success' => false, 'message' => 'Usuario no encontrado']);
    exit;
}

$user = $result->fetch_assoc();

if (password_verify($password, $user['password'])) {
    echo json_encode([
        'success' => true,
        'id' => $user['id'],
        'username' => $user['username'],
        'foto_perfil' => $user['foto_perfil']
    ]);
} else {
    echo json_encode(['success' => false, 'message' => 'Contraseña incorrecta']);
}

$stmt->close();
$conn->close();
?>
