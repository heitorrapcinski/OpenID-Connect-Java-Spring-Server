// Seed: clients → mongodb-client:27017/client_db
// Idempotent: usa updateOne com upsert por clientId

db = db.getSiblingDB('client_db');

const clients = [
  {
    _id:                       'client',
    clientId:                  'client',
    clientSecret:              'secret',
    clientName:                'Test Client',
    dynamicallyRegistered:     false,
    allowIntrospection:        true,
    reuseRefreshToken:         true,
    clearAccessTokensOnRefresh: true,
    accessTokenValiditySeconds:  3600,
    refreshTokenValiditySeconds: null,
    idTokenValiditySeconds:      600,
    scope:        ['openid', 'profile', 'email', 'address', 'phone', 'offline_access'],
    grantTypes:   ['authorization_code', 'implicit', 'refresh_token',
                   'urn:ietf:params:oauth:grant_type:redelegate'],
    responseTypes: ['code', 'token', 'id_token'],
    redirectUris: ['http://localhost/', 'http://localhost:8080/'],
    tokenEndpointAuthMethod: 'client_secret_basic',
    applicationType: 'WEB',
    createdAt: new Date(),
    version: NumberLong(0),
  },
];

clients.forEach(c => {
  db.clients.updateOne(
    { _id: c._id },
    { $setOnInsert: c },
    { upsert: true }
  );
});

print('✓ clients: ' + db.clients.countDocuments() + ' clients');
