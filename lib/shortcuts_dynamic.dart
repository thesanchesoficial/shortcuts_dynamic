import 'dart:async';
import 'package:flutter/services.dart';

/// Classe que representa um atalho do Android
class ShortcutInfo {
  /// ID único do atalho
  final String id;
  
  /// Nome do atalho
  final String name;
  
  /// Caminho do ícone (pode ser asset local ou URL)
  final String iconPath;
  
  /// Nome do pacote da aplicação
  final String? packageName;
  
  /// Nome da activity principal
  final String? activityName;
  
  /// Extras do intent
  final Map<String, dynamic> intentExtra;

  /// Construtor da classe
  const ShortcutInfo({
    required this.id,
    required this.name,
    required this.iconPath,
    this.packageName,
    this.activityName,
    this.intentExtra = const {},
  });

  /// Cria um atalho a partir de um Map
  factory ShortcutInfo.fromMap(Map<String, dynamic> map) {
    return ShortcutInfo(
      id: map['id'] as String,
      name: map['shortLabel'] as String,
      iconPath: map['iconPath'] as String? ?? '',
      packageName: map['packageName'] as String?,
      activityName: map['activityName'] as String?,
      intentExtra: Map<String, dynamic>.from(map['intentExtra'] as Map? ?? {}),
    );
  }

  /// Converte o atalho para um Map
  Map<String, String> toMap() {
    return {
      'id': id,
      'name': name,
      'iconPath': iconPath,
      if (packageName != null) 'packageName': packageName!,
      if (activityName != null) 'activityName': activityName!,
      ...intentExtra.map((key, value) => MapEntry(key, value.toString())),
    };
  }
}

class ShortcutsDynamic {
  static const MethodChannel _channel = MethodChannel('shortcut');

  /// Cria um novo atalho
  static Future<bool> create(ShortcutInfo shortcut) async {
    try {
      final result = await _channel.invokeMethod('create', shortcut.toMap());
      return result == true;
    } on PlatformException catch (e) {
      throw Exception('Erro ao criar atalho: ${e.message}');
    }
  }

  /// Busca um atalho pelo ID
  static Future<bool> search(String id) async {
    try {
      final result = await _channel.invokeMethod('search', {'id': id});
      return result == true;
    } on PlatformException catch (e) {
      throw Exception('Erro ao buscar atalho: ${e.message}');
    }
  }

  /// Lista todos os atalhos existentes
  static Future<List<ShortcutInfo>> list() async {
    try {
      final result = await _channel.invokeMethod('list');
      return (result as List)
          .map((map) => ShortcutInfo.fromMap(Map<String, dynamic>.from(map)))
          .toList();
    } on PlatformException catch (e) {
      throw Exception('Erro ao listar atalhos: ${e.message}');
    }
  }

  /// Remove um atalho pelo ID
  static Future<bool> remove(String id) async {
    try {
      final result = await _channel.invokeMethod('remove', {'id': id});
      return result == true;
    } on PlatformException catch (e) {
      throw Exception('Erro ao remover atalho: ${e.message}');
    }
  }
}
