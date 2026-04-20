<?php
require_once 'config.php';

$id = $_POST['id'] ?? '';
$user_id = $_POST['user_id'] ?? '';
$nombre = $_POST['nombre'] ?? '';
$plataforma = $_POST['plataforma'] ?? '';
$estado = $_POST['estado'] ?? '';
$horas = floatval($_POST['horas_jugadas'] ?? 0);
$puntuacion = intval($_POST['puntuacion'] ?? 0);
$notas = $_POST['notas'] ?? '';
$fecha = $_POST['fecha_ultima_sesion'] ?? '';

if (empty($id) || empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Faltan campos']);
    exit;
}

$stmt = $conn->prepare("UPDATE juegos SET nombre=?, plataforma=?, estado=?, horas_jugadas=?, puntuacion=?, notas=?, fecha_ultima_sesion=? WHERE id=? AND user_id=?");
$stmt->bind_param("sssdissii", $nombre, $plataforma, $estado, $horas, $puntuacion, $notas, $fecha, $id, $user_id);

if ($stmt->execute()) {
    echo json_encode(['success' => true]);
} else {
    echo json_encode(['success' => false, 'message' => $conn->error]);
}

$stmt->close();
$conn->close();
?>
