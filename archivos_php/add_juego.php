<?php
require_once 'config.php';

$user_id = $_POST['user_id'] ?? '';
$nombre = $_POST['nombre'] ?? '';
$plataforma = $_POST['plataforma'] ?? '';
$estado = $_POST['estado'] ?? '';
$horas = floatval($_POST['horas_jugadas'] ?? 0);
$puntuacion = intval($_POST['puntuacion'] ?? 0);
$notas = $_POST['notas'] ?? '';
$fecha = $_POST['fecha_ultima_sesion'] ?? '';

if (empty($user_id) || empty($nombre)) {
    echo json_encode(['success' => false, 'message' => 'Faltan campos obligatorios']);
    exit;
}

$stmt = $conn->prepare("INSERT INTO juegos (user_id, nombre, plataforma, estado, horas_jugadas, puntuacion, notas, fecha_ultima_sesion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
$stmt->bind_param("isssdiss", $user_id, $nombre, $plataforma, $estado, $horas, $puntuacion, $notas, $fecha);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'id' => $conn->insert_id]);
} else {
    echo json_encode(['success' => false, 'message' => $conn->error]);
}

$stmt->close();
$conn->close();
?>
