import os
import argparse
import sys

def collect_project_files(project_path, extensions=None, exclude_dirs=None, follow_symlinks=False):
    # Расширения, релевантные для Android Studio
    if extensions is None:
        extensions = {
            '.kt', '.java', '.xml', '.gradle', '.kts', '.properties', '.pro',
            '.json', '.yaml', '.yml', '.md', '.txt', '.cfg', '.ini', '.toml',
            '.sql', '.sh', '.bat', '.cmake', '.mk', '.c', '.cpp', '.h', '.proto'
        }
    
    # Папки сборки/IDE, которые не нужны для анализа исходников
    if exclude_dirs is None:
        exclude_dirs = {
            'build', '.gradle', '.idea', 'captures', 'out', '.git',
            '__pycache__', 'node_modules', '.venv', 'dist', 'venv', 'env'
        }
        
    collected_files = []
    skipped_files = []

    def onerror(err):
        skipped_files.append(f"⛔ Нет доступа к папке: {err.filename} ({err.strerror})")

    for root, dirs, files in os.walk(project_path, followlinks=follow_symlinks, onerror=onerror):
        # Убираем исключаемые директории "на лету" (прерывает рекурсию в них)
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            file_path = os.path.join(root, file)
            _, file_ext = os.path.splitext(file)
            file_ext = file_ext.lower()
            
            # '*' = брать все файлы без фильтра по расширению
            if extensions == {'*'} or file_ext in extensions:
                # Пропускаем бинарные ресурсы (изображения, шрифты и т.д.)
                if file_ext in {'.png', '.jpg', '.jpeg', '.gif', '.webp', '.ttf', '.otf', '.wav', '.mp3', '.apk', '.aar'}:
                    skipped_files.append(f"🖼 Пропущено (бинарник): {os.path.relpath(file_path, project_path)}")
                    continue
                collected_files.append(file_path)
            else:
                skipped_files.append(f"📄 Пропущено (расширение): {os.path.relpath(file_path, project_path)}")
                
    return collected_files, skipped_files

def create_analysis_file(files, output_path, project_path):
    processed = 0
    with open(output_path, 'w', encoding='utf-8') as outfile:
        outfile.write(f"=== Проект Android: {project_path} ===\n")
        outfile.write(f"Всего текстовых файлов: {len(files)}\n\n")
        
        for file_path in files:
            try:
                with open(file_path, 'r', encoding='utf-8', errors='replace') as infile:
                    content = infile.read()
                    relative_path = os.path.relpath(file_path, project_path)
                    outfile.write(f"\n=== Файл: {relative_path} ===\n")
                    outfile.write(content)
                    outfile.write("\n\n--- КОНЕЦ ФАЙЛА ---\n")
                    processed += 1
            except Exception as e:
                print(f"⚠️ Ошибка чтения {file_path}: {e}", file=sys.stderr)
    
    return processed

def main():
    parser = argparse.ArgumentParser(description='Сбор Android-проекта в единый файл для анализа LLM')
    parser.add_argument('project_path', type=str, help='Путь к корню Android Studio проекта')
    parser.add_argument('--output', '-o', type=str, default='android_analysis.txt', 
                        help='Имя выходного файла (по умолчанию: android_analysis.txt)')
    parser.add_argument('--extensions', '-e', nargs='+', 
                        help='Расширения для включения. Используйте "*" для всех файлов.')
    parser.add_argument('--exclude', '-x', nargs='+', 
                        help='Директории для исключения (по умолчанию: build/.idea/.gradle и др.)')
    parser.add_argument('--follow-symlinks', action='store_true',
                        help='Обходить символические ссылки на папки')
    parser.add_argument('--list-only', action='store_true',
                        help='Только показать найденные файлы без генерации output')
    parser.add_argument('--verbose', '-v', action='store_true',
                        help='Подробный вывод процесса обхода')
    
    args = parser.parse_args()
    
    project_path = os.path.abspath(args.project_path)
    output_path = os.path.abspath(args.output)
    
    if not os.path.isdir(project_path):
        print(f"❌ Ошибка: Директория {project_path} не существует", file=sys.stderr)
        sys.exit(1)
    
    if args.verbose:
        print(f"📁 Сканирование: {project_path}")
    
    ext_set = set(args.extensions) if args.extensions else None
    files, skipped = collect_project_files(
        project_path,
        extensions=ext_set,
        exclude_dirs=set(args.exclude) if args.exclude else None,
        follow_symlinks=args.follow_symlinks
    )
    
    if skipped:
        print(f"\n⚠️ Пропущено: {len(skipped)} файлов/папок")
        if args.verbose:
            for msg in skipped[:15]:
                print(f"  {msg}")
            if len(skipped) > 15:
                print(f"  ... и ещё {len(skipped) - 15}")
    
    if not files:
        print("ℹ️ Файлы не найдены. Проверьте структуру проекта или используйте --extensions *")
        sys.exit(0)
    
    print(f"\n✅ Найдено текстовых файлов: {len(files)}")
    
    if args.list_only:
        print("\n📋 Список файлов:")
        for f in files:
            print(f"  {os.path.relpath(f, project_path)}")
        return
    
    output_path = os.path.abspath(args.output)
    print(f"📝 Создание {output_path}...")
    processed = create_analysis_file(files, output_path, project_path)
    print(f"🎉 Готово! Обработано {processed} файлов. Файл сохранён в: {output_path}")

if __name__ == '__main__':
    main()