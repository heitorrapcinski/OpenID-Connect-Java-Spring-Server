// Seed: system_scopes → mongodb-scope:27017/scope_db
// Idempotent: usa updateOne com upsert, nunca duplica

db = db.getSiblingDB('scope_db');

const scopes = [
  { _id: 'openid',         value: 'openid',         description: 'log in using your identity',  icon: 'user',     defaultScope: true,  restricted: false },
  { _id: 'profile',        value: 'profile',        description: 'basic profile information',    icon: 'list-alt', defaultScope: true,  restricted: false },
  { _id: 'email',          value: 'email',          description: 'email address',                icon: 'envelope', defaultScope: true,  restricted: false },
  { _id: 'address',        value: 'address',        description: 'physical address',             icon: 'home',     defaultScope: false, restricted: false },
  { _id: 'phone',          value: 'phone',          description: 'telephone number',             icon: 'bell',     defaultScope: false, restricted: false },
  { _id: 'offline_access', value: 'offline_access', description: 'offline access',               icon: 'time',     defaultScope: false, restricted: false },
];

scopes.forEach(s => {
  db.system_scopes.updateOne(
    { _id: s._id },
    { $setOnInsert: s },
    { upsert: true }
  );
});

print('✓ system_scopes: ' + db.system_scopes.countDocuments() + ' scopes');
