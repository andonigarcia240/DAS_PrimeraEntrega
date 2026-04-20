<?php
require_once 'config.php';

$username = $_POST['username'] ?? '';
$email = $_POST['email'] ?? '';
$password = $_POST['password'] ?? '';

if (empty($username) || empty($email) || empty($password)) {
    echo json_encode(['success' => false, 'message' => 'Faltan campos']);
    exit;
}

$password_hash = password_hash($password, PASSWORD_BCRYPT);

$stmt = $conn->prepare("INSERT INTO usuarios (username, email, password) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $username, $email, $password_hash);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Usuario registrado correctamente']);
} else {
    if ($conn->errno == 1062) {
        echo json_encode(['success' => false, 'message' => 'El usuario o email ya existe']);
    } else {
        echo json_encode(['success' => false, 'message' => 'Error al registrar']);
    }
}

$stmt->close();
$conn->close();
?>
