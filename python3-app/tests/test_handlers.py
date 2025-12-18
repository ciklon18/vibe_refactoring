import urllib.parse
import os
import sys

from tornado import testing
from tornado.escape import json_decode

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from main import init_db, make_app
from tests.fake_redis import InMemoryRedis


def url_encode(data: dict) -> bytes:
    return urllib.parse.urlencode(data).encode()


class _BaseAppTest(testing.AsyncHTTPTestCase):
    __test__ = False

    def runTest(self):
        """Required by unittest when no default test method is provided."""
        pass
    def setUp(self):
        self.redis = InMemoryRedis()
        init_db(self.redis)
        super().setUp()

    def get_app(self):
        return make_app(self.redis)


class TestHospitalHandler(_BaseAppTest):
    __test__ = True
    def test_hospital_requires_name_and_address(self):
        response = self.fetch(
            "/hospital",
            method="POST",
            body=url_encode({"name": "", "address": ""}),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

        assert response.code == 400
        assert b"Hospital name and address required" in response.body

    def test_create_hospital(self):
        response = self.fetch(
            "/hospital",
            method="POST",
            body=url_encode(
                {
                    "name": "City Clinic",
                    "address": "Main street",
                    "phone": "123",
                    "beds_number": "42",
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

        assert response.code == 200
        assert b"OK: ID 0 for City Clinic" in response.body
        assert self.redis.hget("hospital:0", "name") == b"City Clinic"


class TestPatientHandler(_BaseAppTest):
    __test__ = True
    def test_patient_sex_validation(self):
        response = self.fetch(
            "/patient",
            method="POST",
            body=url_encode(
                {
                    "surname": "Ivanov",
                    "born_date": "1999-01-01",
                    "sex": "X",
                    "mpn": "1234",
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

        assert response.code == 400
        assert b"Sex must be 'M' or 'F'" in response.body


class TestStatsHandler(_BaseAppTest):
    __test__ = True
    def _create_hospital(self):
        return self.fetch(
            "/hospital",
            method="POST",
            body=url_encode(
                {
                    "name": "City Clinic",
                    "address": "Main street",
                    "phone": "123",
                    "beds_number": "42",
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

    def _create_patient(self):
        return self.fetch(
            "/patient",
            method="POST",
            body=url_encode(
                {
                    "surname": "Petrov",
                    "born_date": "1990-01-01",
                    "sex": "M",
                    "mpn": "MPN1",
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

    def _create_doctor(self, hospital_id=""):
        return self.fetch(
            "/doctor",
            method="POST",
            body=url_encode(
                {
                    "surname": "Sidorov",
                    "profession": "therapist",
                    "hospital_ID": hospital_id,
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

    def _link_doctor_patient(self):
        return self.fetch(
            "/doctor-patient",
            method="POST",
            body=url_encode({"doctor_ID": "0", "patient_ID": "0"}),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

    def _create_diagnosis(self):
        return self.fetch(
            "/diagnosis",
            method="POST",
            body=url_encode(
                {
                    "patient_ID": "0",
                    "type": "flu",
                    "information": "Bed rest",
                }
            ),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )

    def test_stats_reflect_created_entities(self):
        self._create_hospital()
        self._create_patient()
        self._create_doctor(hospital_id="0")
        self._link_doctor_patient()
        self._create_diagnosis()

        response = self.fetch("/stats")

        assert response.code == 200
        stats = json_decode(response.body)
        assert stats == {
            "hospitals": 1,
            "doctors": 1,
            "patients": 1,
            "diagnoses": 1,
            "doctor_patient_links": 1,
        }
