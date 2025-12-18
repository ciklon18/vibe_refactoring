class InMemoryRedis:
    def __init__(self):
        self.store = {}

    def _encode(self, value):
        if value is None:
            return None
        if isinstance(value, bytes):
            return value
        return str(value).encode()

    def set(self, key, value):
        self.store[key] = self._encode(value)

    def get(self, key):
        return self.store.get(key)

    def hset(self, name, key, value):
        hash_value = self.store.setdefault(name, {})
        if not isinstance(hash_value, dict):
            raise TypeError("Hash value is not a dict")

        is_new_field = key not in hash_value
        hash_value[key] = self._encode(value)
        return 1 if is_new_field else 0

    def hgetall(self, name):
        value = self.store.get(name)
        if isinstance(value, dict):
            return value
        return {}

    def hget(self, name, key):
        value = self.store.get(name)
        if isinstance(value, dict):
            return value.get(key)
        return None

    def smembers(self, name):
        value = self.store.get(name)
        if isinstance(value, set):
            return value
        return set()

    def sadd(self, name, value):
        current = self.store.setdefault(name, set())
        if not isinstance(current, set):
            raise TypeError("Set value is not a set")
        current.add(self._encode(value))
        return 1

    def incr(self, name):
        current = self.store.get(name, b"0")
        if isinstance(current, bytes):
            current = int(current.decode())
        elif isinstance(current, str):
            current = int(current)
        else:
            current = int(current)
        current += 1
        self.store[name] = self._encode(current)
        return current
