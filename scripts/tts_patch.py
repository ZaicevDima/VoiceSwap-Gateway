import torch

# Сохраняем оригинальную функцию
original_load = torch.load

# Создаём новую функцию с weights_only=False по умолчанию
def patched_load(f, map_location=None, pickle_module=None, *, weights_only=False, **pickle_load_args):
    return original_load(f, map_location, pickle_module, weights_only=weights_only, **pickle_load_args)

# Применяем патч глобально
torch.load = patched_load

print("[INFO] PyTorch load patched: weights_only=False by default")