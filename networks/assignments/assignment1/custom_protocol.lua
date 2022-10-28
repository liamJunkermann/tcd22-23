csu33031_protocol = Proto("csu33031", "File Transfer Protocol")
packet_type = ProtoField.uint8("csu33031.packet_type", "PacketType", base.DEC)
packet_name = ProtoField.string("csu33031.packet_name", "PacketName")
source_idx = ProtoField.uint8("csu33031.source_idx", "SourceIdx", base.DEC)
message0 = ProtoField.uint8("csu33031.message0", "message0", base.DEC)
message1 = ProtoField.uint8("csu33031.message1", "message1", base.DEC)
message2 = ProtoField.uint8("csu33031.message2", "message2", base.DEC)
data_payload = ProtoField.string("csu33031.data_payload", "DataPayload")

csu33031_protocol.fields = { packet_type, packet_name, source_idx, message0, message1, message2, data_payload }

function get_packet_type(type)
    local type_name = "unknown"
    if type == 0 then type_name = "FILEREQ"
    elseif type == 1 then type_name = "FWDFILEREQ"
    elseif type == 2 then type_name = "FILERES"
    elseif type == 3 then type_name = "TESTPKT"
    elseif type == 4 then type_name = "REGCLIENT"
    elseif type == 5 then type_name = "REGWORKER"
    elseif type == 6 then type_name = "REGACK"
    elseif type == 7 then type_name = "FWDFILERES"
    elseif type == 8 then type_name = "FILEACK"
    end
    return type_name
end

function csu33031_protocol.dissector(buffer, pinfo, tree)
    local length = buffer:len()
    if length == 0 then return end

    pinfo.cols.protocol = csu33031_protocol.name
    local subtree = tree:add(csu33031_protocol, buffer(), "LJ Protocol Data")

    local type = buffer(0, 1):le_uint()
    local type_name = get_packet_type(type)

    subtree:add_le(packet_type, buffer(0, 1)) -- :append_text(" (" .. type_name .. ")")
    subtree:add_le(packet_name, type_name)


    if (type_name == "FILERES" or type_name == "FWDFILEREQ" or type_name == "FILEACK") then
        subtree:add_le(source_idx, buffer(1, 1))
    end

    if (type_name == "FILEACK") then
        subtree:add_le(message0, buffer(2, 1))
        subtree:add_le(message1, buffer(3, 1))
        return
    end

    if (length > 2) then
        if (type_name == "FILERES" or type_name == "FWDFILERES") then
            subtree:add_le(message0, buffer(1, 1))
            subtree:add_le(message1, buffer(2, 1))
            subtree:add_le(message2, buffer(3, 1))
            subtree:add_le(data_payload, buffer(5, length - 5):string())
            return
        else
            local content = buffer(2, length - 2):string()
            subtree:add_le(data_payload, buffer(2, string.len(content)))
            return
        end
    end
end

local udp_port = DissectorTable.get('udp.port')
udp_port:add(50000, csu33031_protocol)
