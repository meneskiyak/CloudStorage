package tr.edu.duzce.mf.bm.cloudstorage.core.enums;

/**
 * Kullanıcı rollerini ve yetkilerini tanımlayan enum.
 * ADMIN = 0, USER = 1 olarak ID'lendirilmiştir.
 */
public enum Role {
    ADMIN(0),
    USER(1);

    private final int id;

    Role(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Role fromId(int id) {
        for (Role role : Role.values()) {
            if (role.getId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("Geçersiz rol ID: " + id);
    }
}
