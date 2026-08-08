import { Injectable } from '@nestjs/common';
import { Unit } from '@prisma/client';

interface GS1Data {
  gtin?: string;           // AI (01)
  lot?: string;            // AI (10)
  expiryDate?: string;     // AI (17) - format YYMMDD
  serialNumber?: string;   // AI (21)
  variableWeight?: number; // AI (310n) - weight in kg
  rawPayload?: string;     // Original string from scanner
}

interface DigitalLink {
  gtin: string;
  qualifiers?: Record<string, string>;
}

@Injectable()
export class GS1Parser {
  /**
   * Parse GS1 DataMatrix or Digital Link string
   * Supports:
   * - GS1 DataMatrix (starts with FNC1: ]= or ]>)
   * - GS1 Digital Link URI (https://id.gs1.org/...)
   * - Plain GTIN (13 or 14 digits)
   */
  parse(input: string): GS1Data {
    if (!input?.trim()) {
      return {};
    }

    const cleanInput = input.trim();
    
    // Check if it's a Digital Link URI
    if (cleanInput.startsWith('https://id.gs1.org/')) {
      return this.parseDigitalLink(cleanInput);
    }
    
    // Check if it's GS1 DataMatrix (starts with FNC1)
    if (cleanInput.startsWith(']=') || cleanInput.startsWith(']>')) {
      return this.parseGS1DataMatrix(cleanInput.substring(2));
    }
    
    // Check if it's a plain GTIN (13 or 14 digits)
    if (/^\d{13,14}$/.test(cleanInput)) {
      return { gtin: cleanInput };
    }
    
    // If we can't parse it, return empty but keep raw payload for debugging
    return { rawPayload: cleanInput };
  }

  private parseGS1DataMatrix(data: string): GS1Data {
    const result: GS1Data = {};
    let pos = 0;
    
    while (pos < data.length) {
      // AI is 2-4 digits
      const aiMatch = data.substring(pos).match(/^(\d{2,4})/);
      if (!aiMatch) break;
      
      const ai = aiMatch[1];
      pos += ai.length;
      
      // Determine field length based on AI
      let fieldLength: number | null = null;
      let isVariable = false;
      
      switch (ai) {
        case '01': // GTIN
          fieldLength = 14;
          break;
        case '10': // BATCH/LOT
          fieldLength = null; // variable up to 20
          break;
        case '17': // EXPIRY DATE
          fieldLength = 6; // YYMMDD
          break;
        case '21': // SERIAL
          fieldLength = null; // variable up to 20
          break;
        case '3100': case '3101': case '3102': case '3103': 
        case '3104': case '3105': case '3106': case '3107':
        case '3108': case '3109': // NET WEIGHT kg
          fieldLength = 6; // n..6 + 1 decimal
          isVariable = true;
          break;
        default:
          // Unknown AI, skip 1 character and continue
          pos++;
          continue;
      }
      
      // Extract field value
      let value: string;
      if (fieldLength !== null) {
        value = data.substring(pos, pos + fieldLength);
        pos += fieldLength;
      } else {
        // Variable length - next AI starts with digit or end of string
        const nextAiMatch = data.substring(pos).match(/^\d{2,4}/);
        if (nextAiMatch) {
          value = data.substring(pos, pos + nextAiMatch.index);
          pos += value.length;
        } else {
          value = data.substring(pos);
          pos = data.length;
        }
      }
      
      // Process value based on AI
      switch (ai) {
        case '01':
          result.gtin = value;
          break;
        case '10':
          result.lot = value;
          break;
        case '17':
          // Convert YYMMDD to YYYY-MM-DD
          if (/^\d{6}$/.test(value)) {
            const yy = parseInt(value.substring(0, 2), 10);
            const mm = value.substring(2, 4);
            const dd = value.substring(4, 6);
            const year = yy < 50 ? 2000 + yy : 1900 + yy;
            result.expiryDate = `${year}-${mm}-${dd}`;
          }
          break;
        case '21':
          result.serialNumber = value;
          break;
        case '3100': case '3101': case '3102': case '3103':
        case '3104': case '3105': case '3106': case '3107':
        case '3108': case '3109':
          // Variable weight: first digit is decimal position, rest is weight
          if (value.length >= 2) {
            const decimalPos = parseInt(value.substring(0, 1), 10);
            const weightValue = parseInt(value.substring(1), 10);
            result.variableWeight = weightValue / Math.pow(10, decimalPos);
          }
          break;
      }
    }
    
    return result;
  }

  private parseDigitalLink(uri: string): GS1Data {
    try {
      const url = new URL(uri);
      const result: GS1Data = {};
      
      // Extract GTIN from path: /01/GTIN/...
      const pathParts = url.pathname.split('/').filter(p => p);
      for (let i = 0; i < pathParts.length; i++) {
        if (pathParts[i] === '01' && i + 1 < pathParts.length) {
          result.gtin = pathParts[i + 1];
          break;
        }
      }
      
      // Extract qualifiers (AI-value pairs)
      for (let i = 0; i < pathParts.length; i++) {
        const part = pathParts[i];
        if (/^\d{2,4}$/.test(part)) {
          const ai = part;
          const value = pathParts[i + 1];
          if (value && !/^\d{2,4}$/.test(value)) {
            // This is a qualifier value
            // We could parse specific AIs here if needed
            // For now, just store as raw
          }
        }
      }
      
      return result;
    } catch (e) {
      return { rawPayload: uri };
    }
  }
}