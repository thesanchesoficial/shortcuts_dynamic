# Shortcuts Dynamic

[English](#english) | [Português](#português)

## English

A Flutter plugin that allows you to create, manage, and remove Android shortcuts dynamically. This plugin provides a simple and efficient way to handle Android shortcuts in your Flutter applications.

### Features

- Create shortcuts with custom icons (local assets or web URLs)
- Remove shortcuts programmatically
- List existing shortcuts
- Search for specific shortcuts
- Support for Android 8.0 (API level 26) and above
- Automatic icon download and caching for web images
- Type-safe shortcut management with `ShortcutInfo` class

### Benefits

1. **Easy Integration**: Simple API that integrates seamlessly with Flutter applications
2. **Type Safety**: Uses a dedicated `ShortcutInfo` class for better type checking and code completion
3. **Web Image Support**: Automatically downloads and caches icons from web URLs
4. **Modern Android Features**: Supports the latest Android shortcut APIs
5. **Error Handling**: Comprehensive error handling and user feedback
6. **Memory Efficient**: Automatically manages temporary files and cache

### Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  shortcuts_dynamic: ^1.0.0
```

### Usage

1. Import the package:

```dart
import 'package:shortcuts_dynamic/shortcuts_dynamic.dart';
```

2. Create a shortcut:

```dart
final shortcut = ShortcutInfo(
  id: 'unique_id',
  name: 'My Shortcut',
  iconPath: 'https://example.com/icon.png', // Web URL or asset path
  packageName: 'com.example.app',
  activityName: 'com.example.app.MainActivity',
  intentExtra: {'route': '/home'},
);

await ShortcutsDynamic.create(shortcut);
```

3. List shortcuts:

```dart
final shortcuts = await ShortcutsDynamic.list();
```

4. Remove a shortcut:

```dart
await ShortcutsDynamic.remove('shortcut_id');
```

5. Search for a shortcut:

```dart
final exists = await ShortcutsDynamic.search('shortcut_id');
```

### Example

Check the `example` directory for a complete working example.

## Português

Um plugin Flutter que permite criar, gerenciar e remover atalhos do Android dinamicamente. Este plugin fornece uma maneira simples e eficiente de lidar com atalhos do Android em suas aplicações Flutter.

### Funcionalidades

- Criar atalhos com ícones personalizados (assets locais ou URLs da web)
- Remover atalhos programaticamente
- Listar atalhos existentes
- Buscar atalhos específicos
- Suporte para Android 8.0 (API level 26) e superior
- Download e cache automático de ícones da web
- Gerenciamento de atalhos com verificação de tipos usando a classe `ShortcutInfo`

### Benefícios

1. **Fácil Integração**: API simples que se integra perfeitamente com aplicações Flutter
2. **Segurança de Tipos**: Usa uma classe `ShortcutInfo` dedicada para melhor verificação de tipos e autocompletar
3. **Suporte a Imagens Web**: Baixa e armazena automaticamente ícones de URLs da web
4. **Recursos Modernos do Android**: Suporta as mais recentes APIs de atalhos do Android
5. **Tratamento de Erros**: Tratamento abrangente de erros e feedback ao usuário
6. **Eficiência de Memória**: Gerencia automaticamente arquivos temporários e cache

### Instalação

Adicione isto ao arquivo `pubspec.yaml` do seu pacote:

```yaml
dependencies:
  shortcuts_dynamic: ^1.0.0
```

### Como Usar

1. Importe o pacote:

```dart
import 'package:shortcuts_dynamic/shortcuts_dynamic.dart';
```

2. Crie um atalho:

```dart
final shortcut = ShortcutInfo(
  id: 'id_unico',
  name: 'Meu Atalho',
  iconPath: 'https://exemplo.com/icone.png', // URL da web ou caminho do asset
  packageName: 'com.exemplo.app',
  activityName: 'com.exemplo.app.MainActivity',
  intentExtra: {'route': '/home'},
);

await ShortcutsDynamic.create(shortcut);
```

3. Liste os atalhos:

```dart
final atalhos = await ShortcutsDynamic.list();
```

4. Remova um atalho:

```dart
await ShortcutsDynamic.remove('id_do_atalho');
```

5. Busque um atalho:

```dart
final existe = await ShortcutsDynamic.search('id_do_atalho');
```

### Exemplo

Verifique o diretório `example` para um exemplo completo e funcional.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

