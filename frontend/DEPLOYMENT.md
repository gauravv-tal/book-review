# Frontend Deployment (S3 + CloudFront)

This document explains how to build the React frontend and deploy it to the S3 bucket and CloudFront distribution provisioned by Terraform.

## 1) Configure backend API base URL

The app reads the backend base URL from the environment variable `REACT_APP_API_BASE` at build time.

We do NOT commit real env files. Instead, copy the example file and edit locally:

```bash
# from frontend/
cp .env.production.example .env.production
# Edit the value to your backend ALB URL (no trailing slash)
# Example:
# REACT_APP_API_BASE=http://book-review-alb-1256559597.ap-south-1.elb.amazonaws.com
```

Alternatively, you can set the variable inline when running the build command (without creating `.env.production`):

```bash
REACT_APP_API_BASE=http://<your-alb-dns> npm run build
```

Notes:
- `.env.production` is gitignored; do not commit it.
- In local development, the CRA `proxy` (package.json) points to `http://localhost:8080`.

## 2) Build the app

From the `frontend/` directory:

```bash
npm ci
npm run build
```

This creates the production assets in the `build/` directory.

## 3) Deploy to S3

The Terraform module created an S3 bucket (see Terraform output `frontend_bucket_name`). Sync the `build/` output to that bucket (requires AWS CLI to be configured for the target account/region):

```bash
aws s3 sync build/ s3://<frontend_bucket_name>/ --delete --cache-control max-age=31536000,public
```

Replace `<frontend_bucket_name>` with the value from `terraform output`.

## 4) Invalidate CloudFront cache

After uploading, invalidate the CloudFront distribution so users see the new version immediately (replace `<distribution_id>`):

```bash
aws cloudfront create-invalidation --distribution-id <distribution_id> --paths "/*"
```

You can get the Distribution ID from the AWS Console or from Terraform state/outputs if exposed.

## 5) Verify

- Open the CloudFront domain (Terraform output `frontend_cloudfront_domain`).
- Use browser dev tools to confirm network calls go to `REACT_APP_API_BASE`.
- If you see CORS errors, either enable CORS in the backend for your CloudFront domain or configure CloudFront to route API paths to the ALB.

## Troubleshooting

- If API calls still go to localhost:
  - Ensure `REACT_APP_API_BASE` was set at build time (React embeds it into the build).
- If you get 403 AccessDenied on S3 assets:
  - Ensure the Terraform-provided bucket policy and OAC are intact, and you uploaded to the correct bucket.
- If you need to change the backend URL:
  - Update `.env.production` and rebuild + redeploy.
