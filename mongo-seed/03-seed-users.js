// Seed: user_info → mongodb-oidc:27017/oidc_db
// Idempotent: usa updateOne com upsert por sub
// NOTA: senhas são gerenciadas pelo authorization-server (Spring Security).
//       Este seed popula apenas os perfis OIDC (UserInfo).

db = db.getSiblingDB('oidc_db');

const users = [
  {
    _id:               '90342.ASDFJWFA',
    sub:               '90342.ASDFJWFA',
    preferredUsername: 'admin',
    name:              'Demo Admin',
    givenName:         'Demo',
    familyName:        'Admin',
    email:             'admin@example.com',
    emailVerified:     true,
    version:           NumberLong(0),
  },
  {
    _id:               '01921.FLANRJQW',
    sub:               '01921.FLANRJQW',
    preferredUsername: 'user',
    name:              'Demo User',
    givenName:         'Demo',
    familyName:        'User',
    email:             'user@example.com',
    emailVerified:     true,
    version:           NumberLong(0),
  },
];

users.forEach(u => {
  db.user_info.updateOne(
    { _id: u._id },
    { $setOnInsert: u },
    { upsert: true }
  );
});

print('✓ user_info: ' + db.user_info.countDocuments() + ' users');
