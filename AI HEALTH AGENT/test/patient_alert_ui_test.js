const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const scriptPath = path.join(__dirname, '..', 'web', 'script_2.js');
const source = fs.readFileSync(scriptPath, 'utf8');
const sandbox = {
    console,
    document: {
        addEventListener() {},
        getElementById() { return null; },
        querySelector() { return null; },
        querySelectorAll() { return []; }
    },
    window: {
        addEventListener() {},
        location: { href: '' }
    },
    fetch() {
        return Promise.reject(new Error('network disabled in unit test'));
    },
    setInterval() {},
    Date
};

vm.createContext(sandbox);
vm.runInContext(source, sandbox);

const alert = {
    id: 42,
    status: 'CRITICAL',
    bpm: 132,
    createdAt: '2026-06-19T19:48:13Z',
    hospitalName: 'Arcadia Ridge Medical Centre',
    assignmentStatus: 'ASSIGNED'
};

const viewModel = sandbox.buildPatientAlertViewModel(alert);
assert.strictEqual(viewModel.visible, true);
assert.strictEqual(viewModel.heading, 'Critical patient alert');
assert.strictEqual(viewModel.hospital, 'Arcadia Ridge Medical Centre');
assert.strictEqual(viewModel.heartRate, '132 BPM');
assert.strictEqual(viewModel.status, 'CRITICAL');
assert.ok(viewModel.message.includes('hospital staff'));
assert.ok(viewModel.safetyCopy.includes('not real emergency dispatch'));

assert.strictEqual(sandbox.shouldOpenPatientAlertModal(alert, null), true);
assert.strictEqual(sandbox.shouldOpenPatientAlertModal(alert, 42), false);
assert.strictEqual(sandbox.shouldOpenPatientAlertModal(null, null), false);

console.log('patient alert UI helpers pass');
