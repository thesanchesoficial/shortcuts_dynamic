import 'package:flutter/material.dart';
import 'package:shortcuts_dynamic/shortcuts_dynamic.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Shortcuts Dynamic Example',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const ShortcutExample(),
    );
  }
}

class ShortcutExample extends StatefulWidget {
  const ShortcutExample({super.key});

  @override
  State<ShortcutExample> createState() => _ShortcutExampleState();
}

class _ShortcutExampleState extends State<ShortcutExample> {
  List<ShortcutInfo> _shortcuts = [];
  bool _isLoading = false;
  String _statusMessage = '';

  @override
  void initState() {
    super.initState();
    _loadShortcuts();
  }

  Future<void> _loadShortcuts() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Carregando atalhos...';
    });

    try {
      final shortcuts = await ShortcutsDynamic.list();
      setState(() {
        _shortcuts = shortcuts;
        _statusMessage = shortcuts.isEmpty ? 'Nenhum atalho encontrado' : '';
      });
    } catch (e) {
      setState(() {
        _statusMessage = 'Erro ao carregar atalhos: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _addShortcut() async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Criando atalho...';
    });

    try {
      // Cria um atalho com uma imagem da web
      final shortcut = ShortcutInfo(
        id: 'shortcut_${DateTime.now().millisecondsSinceEpoch}',
        name: 'Atalho ${_shortcuts.length + 1}',
        iconPath: 'https://picsum.photos/200', // URL de uma imagem aleatória
        packageName: 'com.example.shortcuts_dynamic_example',
        activityName: 'com.example.shortcuts_dynamic_example.MainActivity',
        intentExtra: {'route': '/home'},
      );

      final success = await ShortcutsDynamic.create(shortcut);
      if (success) {
        // Adiciona o atalho à lista local
        setState(() {
          _shortcuts.add(shortcut);
          _statusMessage = 'Atalho criado com sucesso!';
        });
      } else {
        setState(() {
          _statusMessage = 'Falha ao criar atalho';
        });
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Erro ao criar atalho: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _removeShortcut(String id) async {
    setState(() {
      _isLoading = true;
      _statusMessage = 'Removendo atalho...';
    });

    try {
      final success = await ShortcutsDynamic.remove(id);
      if (success) {
        // Remove o atalho da lista local
        setState(() {
          _shortcuts.removeWhere((shortcut) => shortcut.id == id);
          _statusMessage = 'Atalho removido com sucesso!';
        });
      } else {
        setState(() {
          _statusMessage = 'Falha ao remover atalho';
        });
      }
    } catch (e) {
      setState(() {
        _statusMessage = 'Erro ao remover atalho: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Shortcuts Dynamic Example'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _shortcuts.isEmpty
              ? Center(child: Text(_statusMessage))
              : ListView.builder(
                  itemCount: _shortcuts.length,
                  itemBuilder: (context, index) {
                    final shortcut = _shortcuts[index];
                    return ListTile(
                      leading: shortcut.iconPath.startsWith('http')
                          ? Image.network(
                              shortcut.iconPath,
                              width: 40,
                              height: 40,
                              errorBuilder: (context, error, stackTrace) =>
                                  const Icon(Icons.error),
                            )
                          : Image.asset(
                              shortcut.iconPath,
                              width: 40,
                              height: 40,
                              errorBuilder: (context, error, stackTrace) =>
                                  const Icon(Icons.error),
                            ),
                      title: Text(shortcut.name),
                      subtitle: Text('ID: ${shortcut.id}'),
                      trailing: IconButton(
                        icon: const Icon(Icons.delete),
                        onPressed: () => _removeShortcut(shortcut.id),
                      ),
                    );
                  },
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addShortcut,
        tooltip: 'Adicionar Atalho',
        child: const Icon(Icons.add),
      ),
    );
  }
}
